package com.trikorasolutions.keycloak.client.bl;

import static java.util.function.UnaryOperator.identity;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

import com.trikorasolutions.keycloak.client.clientresource.KeycloakAuthAdminResource;
import com.trikorasolutions.keycloak.client.clientresource.KeycloakAuthorizationResource;
import com.trikorasolutions.keycloak.client.dto.KeycloakUserRepresentation;
import com.trikorasolutions.keycloak.client.dto.RoleRepresentation;
import com.trikorasolutions.keycloak.client.dto.TrikoraGroupRepresentation;
import com.trikorasolutions.keycloak.client.dto.UserRepresentation;
import com.trikorasolutions.keycloak.client.exception.ArgumentsFormatException;
import com.trikorasolutions.keycloak.client.exception.ClientNotFoundException;
import com.trikorasolutions.keycloak.client.exception.DuplicatedRoleException;
import com.trikorasolutions.keycloak.client.exception.DuplicatedUserException;
import com.trikorasolutions.keycloak.client.exception.InvalidTokenException;
import com.trikorasolutions.keycloak.client.exception.NoSuchRoleException;
import com.trikorasolutions.keycloak.client.exception.NoSuchUserException;
import io.restassured.RestAssured;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniJoin.Builder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.keycloak.representations.AccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public final class KeycloakClientLogic {

  private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakClientLogic.class);
  static final String BEARER = "Bearer ";
  static final String GRANT_TYPE = "implicit";
  static final String GRANT_TYPE_PS = "password";

  @ConfigProperty(name = "trikora.keycloak.buffer-size")
  private Integer KC_BUFFER_SIZE;

  @ConfigProperty(name = "quarkus.oidc.auth-server-url")
  private String KC_URL;

  @RestClient
  private KeycloakAuthAdminResource keycloakClient;

  @RestClient
  private KeycloakAuthorizationResource keycloakUserClient;

  @Inject
  private KeycloakGroupLogic blGroup;

  /******************************* SYSTEM FUNCTIONS *******************************/
  /**
   * Get the access token from the system
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param keycloakClientId id of the client (service name).
   * @param secret           system token that is going to be used for authenticate the CLI.
   * @return an access token provided by the keycloak SecurityIdentity.
   */
  public Uni<String> getTokenForUser(final String realm, final String keycloakClientId,
      final String secret) {
    LOGGER.debug("Getting token with params [realm: {}, client_id: {}]", realm, keycloakClientId);
    final String tok = RestAssured.given()
        .param("grant_type", "client_credentials")
        .param("client_id", keycloakClientId)
        .param("client_secret", secret)
        .when()
        .post(KC_URL + "/protocol/openid-connect/token")
        .as(AccessTokenResponse.class)
        .getToken();
    return Uni.createFrom().item(tok);

//    return keycloakUserClient.getToken(realm, "client_credentials", keycloakClientId, secret)
//        .onFailure().invoke(ex->LOGGER.warn("ERR KC: {}." + ex))
//        .map(jsonArray -> (jsonArray.size() != 1) ? null : jsonArray.get(0).asJsonObject())
//        .onItem().ifNull().failWith(() -> new TrikoraGenericException("More than one token"))
//        .onItem().ifNotNull()
//        .transform(obj -> obj.getString("access_token"));
  }

  /******************************* USER FUNCTIONS *******************************/

  /**
   * Creates a new user in the Keycloak database. It can throw DuplicatedUserException,
   * InvalidTokenException, ClientNotFoundException or ArgumentsFormatException exceptions.
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param newUser          a UserRepresentation of the user that is going to be created.
   * @return a UserRepresentation of the created user.
   */
  public Uni<KeycloakUserRepresentation> createUser(final String realm, final String token,
      final String keycloakClientId, final UserRepresentation newUser) {
    LOGGER.debug("#createUser(UserRepresentation)...: {}", newUser);
    return keycloakClient.createUser(BEARER + token, realm, GRANT_TYPE, keycloakClientId,
            newUser)
        .onFailure(ClientWebApplicationException.class).transform(ex -> {
          if (ex.getMessage().contains(String.valueOf(CONFLICT.getStatusCode()))) {
            return new DuplicatedUserException(newUser.username);
          } else if (ex.getMessage().contains(String.valueOf(UNAUTHORIZED.getStatusCode()))) {
            return new InvalidTokenException();
          } else if (ex.getMessage().contains(String.valueOf(NOT_FOUND.getStatusCode()))) {
            return new ClientNotFoundException(keycloakClientId, realm);
          } else {
            return new ArgumentsFormatException(
                "The user representation provided to Keycloak is incorrect, with error: "
                    + ex.getMessage());
          }
        }).replaceWith(this.getUserInfo(realm, token, keycloakClientId, newUser.username));
  }

  /**
   * Updated a user in Keycloak. It can throw NoSuchUserException.
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param userName         username of the user that is going to be updated.
   * @param newUser          raw string containing the new user data in the UserRepresentation
   *                         format.
   * @return a UserRepresentation of the updated user.
   */
  public Uni<KeycloakUserRepresentation> updateUser(final String realm, final String token,
      final String keycloakClientId, final String userName, final UserRepresentation newUser) {
    return this.getUserInfoNoEnrich(realm, token, keycloakClientId, userName)
        .map(KeycloakUserRepresentation::getId)
        .call(userId -> keycloakClient.updateUser(BEARER + token, realm, GRANT_TYPE,
            keycloakClientId, userId, newUser))
        .replaceWith(this.getUserInfo(realm, token, keycloakClientId, newUser.username));
  }

  /**
   * Updated a user in Keycloak. It can throw NoSuchUserException.
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param userName         username of the user that is going to be updated.
   * @param password         raw string containing the new user data in the UserRepresentation
   *                         format.
   * @return a UserRepresentation of the updated user.
   */
  public Uni<KeycloakUserRepresentation> resetPassword(final String realm, final String token,
      final String keycloakClientId, final String userName, final String password) {
    return this.getUserInfoNoEnrich(realm, token, keycloakClientId, userName)
        .map(KeycloakUserRepresentation::getId)
        .call(userId -> keycloakClient.resetPassword(BEARER + token, realm, GRANT_TYPE,
            keycloakClientId, userId, UserRepresentation.credentialsFrom(password, Boolean.FALSE)))
        .replaceWith(this.getUserInfo(realm, token, keycloakClientId, userName));
  }

  /**
   * Updated a user in Keycloak. It can throw NoSuchUserException.
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param userName         username of the user that is going to be updated.
   * @param password         raw string containing the new user data in the UserRepresentation
   *                         format.
   * @param isTemporary      indicates if the user must change the password at the first login
   *                         time.
   * @return a UserRepresentation of the updated user.
   */
  public Uni<KeycloakUserRepresentation> resetPassword(final String realm, final String token,
      final String keycloakClientId, final String userName, final String password,
      final Boolean isTemporary) {
    return this.getUserInfoNoEnrich(realm, token, keycloakClientId, userName)
        .map(KeycloakUserRepresentation::getId)
        .call(userId -> keycloakClient.resetPassword(BEARER + token, realm, GRANT_TYPE,
            keycloakClientId, userId, UserRepresentation.credentialsFrom(password, isTemporary)))
        .replaceWith(this.getUserInfo(realm, token, keycloakClientId, userName));
  }

  /**
   * Enables a user in the keycloak DB.
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param userName         name of the user that is desired to enable.
   * @return true is the user is enabled
   */
  public Uni<Boolean> enableUser(final String realm, final String token,
      final String keycloakClientId, final String userName) {
    return this.getUserInfoNoEnrich(realm, token, keycloakClientId, userName)
        .call(user -> keycloakClient.updateUser(BEARER + token, realm, GRANT_TYPE,
            keycloakClientId, user.id, UserRepresentation.from(user).setEnabled(true)))
        .replaceWith(this.getUserInfoNoEnrich(realm, token, keycloakClientId, userName)
            .map(user -> user.enabled));
  }

  /**
   * Disables a user in the keycloak DB.
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param userName         name of the user that is desired to enable.
   * @return true is the user is disable
   */
  public Uni<Boolean> disableUser(final String realm, final String token,
      final String keycloakClientId, final String userName) {
    return this.getUserInfoNoEnrich(realm, token, keycloakClientId, userName)
        .call(user -> keycloakClient.updateUser(BEARER + token, realm, GRANT_TYPE,
            keycloakClientId, user.id, UserRepresentation.from(user).setEnabled(false)))
        .replaceWith(this.getUserInfoNoEnrich(realm, token, keycloakClientId, userName)
            .map(user -> !user.enabled));
  }

  /**
   * Return the UserRepresentation of one user queried by his username. It can throw
   * NoSuchUserException.
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param userName         username of the user witch is going to be searched.
   * @return a UserRepresentation of the user.
   */
  public Uni<KeycloakUserRepresentation> getUserInfo(final String realm, final String token,
      final String keycloakClientId, final String userName) {
    LOGGER.debug("#getUserInfo(String)...{}", userName);

    return this.getUserInfoNoEnrich(realm, token, keycloakClientId, userName)
        .flatMap(user -> this.getUserRolesById(realm, token, keycloakClientId, user.id)
            .map(user::addRoles))   // Enrich with roles
        .flatMap(user -> this.getGroupsForUser(realm, token, keycloakClientId, user.id)
            .map(user::addGroups)); // Enrich with groups
  }

  public Uni<KeycloakUserRepresentation> getUserInfoNoEnrich(final String realm, final String token,
      final String keycloakClientId, final String userName) {
    return keycloakClient.getUserInfo(BEARER + token, realm, GRANT_TYPE, keycloakClientId,
            userName, Boolean.TRUE)
        .map(jsonArray -> (jsonArray.size() != 1) ? null : jsonArray.get(0).asJsonObject())
        .onItem().ifNull().failWith(() -> new NoSuchUserException(userName)).onItem().ifNotNull()
        .transform(KeycloakUserRepresentation::from);
  }

  /**
   * Deletes a user from the Keycloak database. It can throw NoSuchUserException.
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param userName         name of the user that is going to be deleted from the keycloak
   *                         database.
   * @return a True if teh user has been removed form the db or false otherwise.
   */
  public Uni<Boolean> deleteUser(final String realm, final String token,
      final String keycloakClientId, final String userName) {
    LOGGER.debug("#deleteUser(String)...{}", userName);
    return this.getUserInfoNoEnrich(realm, token, keycloakClientId, userName)
        .call(user -> keycloakClient.deleteUser(BEARER + token, realm, GRANT_TYPE,
            keycloakClientId, user.id))
        .map(x -> Boolean.TRUE)
        .onFailure(NoSuchUserException.class).recoverWithItem(Boolean.FALSE);
  }

  /**
   * This method return a list with all the users in the client provided as argument. It makes use
   * of the Keycloak first and the max params, in order to paginate the search. Making recursion
   * with the mutiny, will subscribe the events sequentially.
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @return a JsonArray of Keycloak UserRepresentations.
   */
  public Uni<List<KeycloakUserRepresentation>> listAllUsers(final String realm, final String token,
      final String keycloakClientId) {
    return this.listAllUsersRec(realm, token, keycloakClientId, 0, Integer.MAX_VALUE,
        new ArrayList<>());
  }

  /**
   * This method return a list with the users in the client provided as argument. It makes use of
   * the Keycloak first and the max params, in order to paginate the search. This method is useful
   * to paginate the users
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param first            first user to be fetched
   * @param recCount         number of users to be fetched from the first one
   * @return a JsonArray of Keycloak UserRepresentations.
   */
  public Uni<List<KeycloakUserRepresentation>> listAllUsers(final String realm, final String token,
      final String keycloakClientId, Integer first, Integer recCount) {

    return this.listAllUsersRec(realm, token, keycloakClientId, first, recCount,
        new ArrayList<>());
  }

  private Uni<List<KeycloakUserRepresentation>> listAllUsersRec(final String realm,
      final String token,
      final String keycloakClientId, Integer first, Integer recCount,
      List<KeycloakUserRepresentation> res) {
    LOGGER.debug("#listAllUsersRec(first, usersFetched)...{}-{}", first, res.size());
    return keycloakClient.listAllUsers(BEARER + token, realm, GRANT_TYPE, keycloakClientId, first,
            (KC_BUFFER_SIZE < (recCount - first) ? KC_BUFFER_SIZE : recCount - first))
        .map(KeycloakUserRepresentation::allFrom)
        .flatMap(currentSelection -> {
          res.addAll(currentSelection);
          if (currentSelection.size() < KC_BUFFER_SIZE || res.size() >= recCount) {
            return Uni.createFrom().item(res); // Recursion Base case
          } else {
            return this.listAllUsersRec(realm, token, keycloakClientId, first + KC_BUFFER_SIZE,
                recCount,
                res);
          }
        });
  }

  /**
   * This method return a list with all the groups for the given user
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param userId           id of the user whose groups are going to be fetched.
   * @return a JsonArray of Keycloak UserRepresentations.
   */
  public Uni<List<TrikoraGroupRepresentation>> getGroupsForUser(final String realm,
      final String token,
      final String keycloakClientId, final String userId) {
    return keycloakClient.getUserGroups(BEARER + token, realm, GRANT_TYPE,
            keycloakClientId, userId)
        .map(TrikoraGroupRepresentation::allFrom);
  }

  /******************************* ROLE FUNCTIONS *******************************/
  /**
   * Creates a role in Keycloak
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param newRole          role that is going to be created into the Keycloak database.
   * @return a GroupRepresentation of the new role.
   */
  public Uni<RoleRepresentation> createRole(final String realm, final String token,
      final String keycloakClientId, final RoleRepresentation newRole) {
    return keycloakClient.createRole(BEARER + token, realm, GRANT_TYPE, keycloakClientId,
            newRole)
        .onFailure(ClientWebApplicationException.class).transform(ex -> {
          if (ex.getMessage().contains(String.valueOf(CONFLICT.getStatusCode()))) {
            return new DuplicatedRoleException(newRole.name);
          } else if (ex.getMessage().contains(String.valueOf(UNAUTHORIZED.getStatusCode()))) {
            return new InvalidTokenException();
          } else if (ex.getMessage().contains(String.valueOf(NOT_FOUND.getStatusCode()))) {
            return new ClientNotFoundException(keycloakClientId, realm);
          } else {
            return new ArgumentsFormatException(
                "The role representation provided to Keycloak is incorrect, with error: "
                    + ex.getMessage());
          }
        })
        .replaceWith(this.getRoleInfoNoEnrich(realm, token, keycloakClientId, newRole.name));
  }

  /**
   * Return a list containing all the roles of the realm.
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @return a List of RoleRepresentations of contatinig all the roles in the realm.
   */
  public Uni<List<RoleRepresentation>> listAllRoles(final String realm, final String token,
      final String keycloakClientId) {
    return keycloakClient.getAllRoles(BEARER + token, realm, GRANT_TYPE, keycloakClientId)
        .map(RoleRepresentation::allFrom);
  }

  /**
   * Return information of one role. And enrich it with its members. It can throw
   * NoSuchRoleException.
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param roleName         name of the role that is going to be queried in the Keycloak database.
   * @return a RoleRepresentation of the desired role.
   */
  public Uni<RoleRepresentation> getRoleInfoNoEnrich(final String realm, final String token,
      final String keycloakClientId, final String roleName) {
    return keycloakClient.getRoleInfo(BEARER + token, realm, GRANT_TYPE, keycloakClientId,
            roleName, Boolean.TRUE)
        .map(jsonArray -> (jsonArray.size() != 1) ? null : jsonArray.get(0).asJsonObject())
        .onItem().ifNull().failWith(() -> new NoSuchRoleException(roleName))
        .map(RoleRepresentation::from);
  }

  /**
   * Updates a role in the KC database.
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param roleName         name of the role that is going to be queried in the Keycloak database.
   * @param newRole          role representation of the role that is going to be updated
   * @return a RoleRepresentation of the desired role.
   */
  public Uni<RoleRepresentation> updateRole(final String realm, final String token,
      final String keycloakClientId, final String roleName, final RoleRepresentation newRole) {
    return keycloakClient.updateRole(BEARER + token, realm, GRANT_TYPE, keycloakClientId,
            roleName, newRole)
        .replaceWith(this.getRoleInfoNoEnrich(realm, token, keycloakClientId, newRole.name));
  }

  /**
   * Deletes a role in Keycloak, if the role do not exit in the realm, kc return a 404 with body
   * "Could not find role", this client transforms that exception to false.
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param roleName         name of the role that is desired to be deleted.
   * @return True if the roles has been removed from the DB, False otherwise.
   */
  public Uni<Boolean> deleteRole(final String realm, final String token,
      final String keycloakClientId, final String roleName) {
    return keycloakClient.deleteRole(BEARER + token, realm, GRANT_TYPE,
            keycloakClientId, roleName)
        .map(x -> Boolean.TRUE)
        .onFailure(ClientWebApplicationException.class).recoverWithItem(Boolean.FALSE);

  }

  /**
   * Return a List of RoleRepresentation with all the roles to the User.
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param userName         username of the user witch is going to be searched.
   * @return a List of RoleRepresentation with all the roles assigned to the User.
   */
  public Uni<List<RoleRepresentation>> getUserRoles(final String realm, final String token,
      final String keycloakClientId, final String userName) {
    return this.getUserInfoNoEnrich(realm, token, keycloakClientId, userName)
        .map(userInfo -> userInfo.id)
        .flatMap(userId -> keycloakClient.getUserRoles(BEARER + token, realm, GRANT_TYPE,
            keycloakClientId, userId))
        .map(RoleRepresentation::allFrom);
  }

  /**
   * Return a List of RoleRepresentation with all the roles to the User.
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param id               of the user witch is going to be searched.
   * @return a List of RoleRepresentation with all the roles assigned to the User.
   */
  public Uni<List<RoleRepresentation>> getUserRolesById(final String realm, final String token,
      final String keycloakClientId, final String id) {
    return keycloakClient.getUserRoles(BEARER + token, realm, GRANT_TYPE,
            keycloakClientId, id)
        .map(RoleRepresentation::allFrom);
  }

  /**
   * Get all the users that has the given role assigned (but not effective)
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param role             role name used to query the users
   * @return List of KeycloakUserRepresentation with the desired users
   */
  public Uni<List<KeycloakUserRepresentation>> getAllUsersInAssignedRole(final String realm,
      final String token, final String keycloakClientId, final String role) {

    return keycloakClient.getAllUsersInRole(BEARER + token, realm, GRANT_TYPE, keycloakClientId,
        role).map(KeycloakUserRepresentation::allFrom);
  }

  /**
   * Get all the groups that has the given role assigned (but not effective)
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param role             role name used to query the groups
   * @return List of GroupRepresentation with the desired groups
   */
  public Uni<List<TrikoraGroupRepresentation>> getAllGroupsInAssignedRole(final String realm,
      final String token, final String keycloakClientId, final String role) {

    return keycloakClient.getAllGroupsInRole(BEARER + token, realm, GRANT_TYPE, keycloakClientId,
        role).map(TrikoraGroupRepresentation::allFrom);
  }

  /**
   * Get all the users that has the given role effective, in order to do that, we get all the users
   * which have the given role assigned, and query all the groups that has the role assigned,
   * subsequently, it gets all the users that belongs to the groups and merge all in a set
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param roleName         role name used to query the groups
   * @return Set of KeycloakUserRepresentation with the desired users.
   */
  public Uni<Set<KeycloakUserRepresentation>> getAllUserInEffectiveRole(final String realm,
      final String token, final String keycloakClientId, final String roleName) {

    Uni<List<KeycloakUserRepresentation>> userAssigned = this.getAllUsersInAssignedRole(realm,
        token, keycloakClientId, roleName);
    Uni<List<TrikoraGroupRepresentation>> groupAssigned = this.getAllGroupsInAssignedRole(realm,
        token,
        keycloakClientId, roleName);

    return Uni.combine().all().unis(userAssigned, groupAssigned)
        .combinedWith((users, groups) -> {
          Builder<List<KeycloakUserRepresentation>> builder = Uni.join().builder();
          for (TrikoraGroupRepresentation group : groups) {
            builder.add(blGroup.getGroupMembers(realm, token, keycloakClientId, group.getName()));
          }
          return builder.joinAll().andCollectFailures()
              .map(listOfList -> listOfList.stream()
                  .flatMap(List::stream)
                  .distinct()
                  .collect(Collectors.toList()))
              .map(listWithGroupUsers -> {
                Set<KeycloakUserRepresentation> ret = new LinkedHashSet<>(listWithGroupUsers);
                ret.addAll(users);
                return ret;
              });
        }).flatMap(identity());
  }

}
