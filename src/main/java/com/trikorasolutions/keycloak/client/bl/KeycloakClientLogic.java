package com.trikorasolutions.keycloak.client.bl;

import com.trikorasolutions.keycloak.client.clientresource.KeycloakAuthAdminResource;
import com.trikorasolutions.keycloak.client.clientresource.KeycloakAuthorizationResource;
import com.trikorasolutions.keycloak.client.dto.GroupRepresentation;
import com.trikorasolutions.keycloak.client.dto.KeycloakUserRepresentation;
import com.trikorasolutions.keycloak.client.dto.RoleRepresentation;
import com.trikorasolutions.keycloak.client.dto.UserRepresentation;
import com.trikorasolutions.keycloak.client.exception.*;
import io.restassured.RestAssured;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniJoin.Builder;
import io.smallrye.mutiny.tuples.Tuple2;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.keycloak.representations.AccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.enterprise.context.ApplicationScoped;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.function.UnaryOperator.identity;
import static javax.ws.rs.core.Response.Status.*;

/**
 * Common arguments to all the methods:
 *
 * <ul>
 * <li>realm the realm name in which the users are going to be queried.</li>
 * <li>keycloakSecurityContext keycloakSecurityContext obtained by the session.</li>
 * <li>keycloakClientId id of the client (service name).</li>
 * </ul>
 */
@ApplicationScoped
public class KeycloakClientLogic {

  private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakClientLogic.class);
  private static final String BEARER = "Bearer ";
  private static final String GRANT_TYPE = "implicit";
  private static final String GRANT_TYPE_PS = "password";

  @RestClient
  KeycloakAuthAdminResource keycloakClient;

  @RestClient
  KeycloakAuthorizationResource keycloakUserClient;


  public Uni<String> getTokenForUser(final String realm, final String keycloakClientId,
      final String secret) {
    LOGGER.warn("Getting token with params:\n"
        + "realm:{},\n"
        + "client_id:{}\n"
        + "secrect:{}", realm, keycloakClientId, secret);
    String tok = RestAssured.given()
        .param("grant_type", "client_credentials")
        .param("client_id", keycloakClientId)
        .param("client_secret", secret)
        .when()
        .post("http://localhost:8090/auth/realms/trikorasolutions" + "/protocol/openid-connect/token")
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

  /**
   * Creates a new user in the Keycloak database. It can throw DuplicatedUserException,
   * InvalidTokenException, ClientNotFoundException or ArgumentsFormatException exceptions.
   *
   * @param newUser a UserRepresentation of the user that is going to be created.
   * @return a UserRepresentation of the created user.
   */
  public Uni<KeycloakUserRepresentation> createUser(final String realm, final String token,
      final String keycloakClientId, final UserRepresentation newUser) {
    return keycloakClient.createUser(BEARER + token, realm, GRANT_TYPE, keycloakClientId,
        newUser).onFailure(ClientWebApplicationException.class).transform(ex -> {
      if (ex.getMessage().contains(String.valueOf(CONFLICT.getStatusCode()))) {
        return new DuplicatedUserException(newUser.username);
      } else if (ex.getMessage().contains(String.valueOf(UNAUTHORIZED.getStatusCode()))) {
        return new InvalidTokenException();
      } else if (ex.getMessage().contains(String.valueOf(NOT_FOUND.getStatusCode()))) {
        return new ClientNotFoundException(keycloakClientId, realm);
      } else {
        return new ArgumentsFormatException(
            "The user representation provided to Keycloak is incorrect");
      }
    }).replaceWith(this.getUserInfo(realm, token, keycloakClientId, newUser.username));
  }

  /**
   * Updated a user in Keycloak. It can throw NoSuchUserException.
   *
   * @param userName username of the user that is going to be updated.
   * @param newUser  raw string containing the new user data in the UserRepresentation format.
   * @return a UserRepresentation of the updated user.
   */
  public Uni<KeycloakUserRepresentation> updateUser(final String realm, final String token,
      final String keycloakClientId, final String userName, final UserRepresentation newUser) {
    return this.getUserInfo(realm, token, keycloakClientId, userName)
        .map(userInfo -> userInfo.id).call(
            userId -> keycloakClient.updateUser(BEARER + token, realm, GRANT_TYPE,
                keycloakClientId, userId, newUser))
        .replaceWith(this.getUserInfo(realm, token, keycloakClientId, newUser.username));
  }

  /**
   * Return the UserRepresentation of one user queried by his username. It can throw
   * NoSuchUserException.
   *
   * @param userName username of the user witch is going to be searched.
   * @return a UserRepresentation of the user.
   */
  public Uni<KeycloakUserRepresentation> getUserInfo(final String realm, final String token,
      final String keycloakClientId, final String userName) {

    return keycloakClient.getUserInfo(BEARER + token, realm, GRANT_TYPE, keycloakClientId,
            userName)
        .map(jsonArray -> (jsonArray.size() != 1) ? null : jsonArray.get(0).asJsonObject())
        .onItem().ifNull().failWith(() -> new NoSuchUserException(userName)).onItem().ifNotNull()
        .transform(KeycloakUserRepresentation::from)
        .flatMap(user -> this.getUserRolesById(realm, token, keycloakClientId, user.id)
            .map(user::addRoles)
        );
  }

  /**
   * Deletes a user from the Keycloak database. It can throw NoSuchUserException.
   *
   * @param userName name of the user that is going to be deleted from the keycloak database.
   * @return a UserRepresentation of the deleted user.
   */
  public Uni<Boolean> deleteUser(final String realm, final String token,
      final String keycloakClientId, final String userName) {
    return this.getUserInfo(realm, token, keycloakClientId, userName)
        .call(user -> keycloakClient.deleteUser(BEARER + token, realm, GRANT_TYPE,
            keycloakClientId, user.id)).map(x -> Boolean.TRUE);
  }

  /**
   * This method return a list with all the users in the client provided as argument
   *
   * @return a JsonArray of Keycloak UserRepresentations.
   */
  public Uni<List<KeycloakUserRepresentation>> listAllUsers(final String realm, final String token,
      final String keycloakClientId) {
    return keycloakClient.listAllUsers(BEARER + token, realm, GRANT_TYPE, keycloakClientId)
        .map(KeycloakUserRepresentation::allFrom);
  }

  /**
   * This method return a list with all the groups in the client provided as argument
   *
   * @return a JsonArray of Keycloak UserRepresentations.
   */
  public Uni<List<GroupRepresentation>> listAllGroups(final String realm, final String token,
      final String keycloakClientId) {
    return keycloakClient.listAllGroups(BEARER + token, realm, GRANT_TYPE, keycloakClientId)
        .map(GroupRepresentation::allFrom);
  }

  /**
   * Return information of one group. It can throw NoSuchGroupException.
   *
   * @param groupName name of the group that is going to be queried in the Keycloak database.
   * @return a GroupRepresentation of the desired group.
   */
  public Uni<GroupRepresentation> getGroupInfo(final String realm, final String token,
      final String keycloakClientId, final String groupName) {
    return keycloakClient.getGroupInfo(BEARER + token, realm, GRANT_TYPE, keycloakClientId,
            groupName)
        .map(jsonArray -> (jsonArray.size() != 1) ? null : jsonArray.get(0).asJsonObject())
        .onItem().ifNull().failWith(() -> new NoSuchGroupException(groupName))
        .map(GroupRepresentation::from)
        .flatMap(group -> this.getGroupRolesById(realm, token, keycloakClientId, group.id)
            .map(group::addRoles)
        );
  }

  /**
   * Gets all the users that belongs to a concrete group. It can throw NoSuchGroupException.
   *
   * @param groupName name of the group that is going to be queried.
   * @return a JsonArray of UserRepresentation.
   */
  public Uni<List<KeycloakUserRepresentation>> getUsersForGroup(final String realm,
      final String token, final String keycloakClientId, final String groupName) {
    return this.getGroupInfo(realm, token, keycloakClientId, groupName)
        .map(GroupRepresentation::getId)
        .flatMap(userId -> keycloakClient.getGroupUsers(BEARER + token, realm, GRANT_TYPE,
            keycloakClientId, userId))
        .map(KeycloakUserRepresentation::allFrom);
  }

  /**
   * Return a List of RoleRepresentation with all the roles to the User.
   *
   * @param userName username of the user witch is going to be searched.
   * @return a List of RoleRepresentation with all the roles assigned to the User.
   */
  public Uni<List<RoleRepresentation>> getUserRoles(final String realm, final String token,
      final String keycloakClientId, final String userName) {
    return this.getUserInfo(realm, token, keycloakClientId, userName)
        .map(userInfo -> userInfo.id)
        .flatMap(userId -> keycloakClient.getUserRoles(BEARER + token, realm, GRANT_TYPE,
            keycloakClientId, userId))
        .map(RoleRepresentation::allFrom);
  }

  /**
   * Return a List of RoleRepresentation with all the roles to the User.
   *
   * @param id of the user witch is going to be searched.
   * @return a List of RoleRepresentation with all the roles assigned to the User.
   */
  public Uni<List<RoleRepresentation>> getUserRolesById(final String realm, final String token,
      final String keycloakClientId, final String id) {
    return keycloakClient.getUserRoles(BEARER + token, realm, GRANT_TYPE,
            keycloakClientId, id)
        .map(RoleRepresentation::allFrom);
  }

  /**
   * Return the UserRepresentation of one user queried by his username. It can throw
   * NoSuchUserException.
   *
   * @param userName username of the user witch is going to be searched.
   * @return a UserRepresentation of the user.
   */
  public Uni<List<RoleRepresentation>> getGroupRoles(final String realm, final String token,
      final String keycloakClientId, final String userName) {
    return this.getGroupInfo(realm, token, keycloakClientId, userName)
        .map(GroupRepresentation::getId)
        .flatMap(id -> keycloakClient.getGroupRoles(BEARER + token, realm, GRANT_TYPE,
            keycloakClientId, id))
        .map(RoleRepresentation::allFrom);
  }

  /**
   * Return the UserRepresentation of one user queried by his username. It can throw
   * NoSuchUserException.
   *
   * @param id the id of the group witch is going to be searched.
   * @return a UserRepresentation of the user.
   */
  public Uni<List<RoleRepresentation>> getGroupRolesById(final String realm, final String token,
      final String keycloakClientId, final String id) {
    return keycloakClient.getGroupRoles(BEARER + token, realm, GRANT_TYPE,
            keycloakClientId, id)
        .map(RoleRepresentation::allFrom);
  }

  /**
   * Add a user to a group. It can throw NoSuchGroupException or NoSuchUserException exceptions.
   *
   * @param userName  name of the user that is going to be added.
   * @param groupName name of the group where the user will belong to.
   * @return a UserRepresentation of the user that has been added to the group.
   */
  public Uni<KeycloakUserRepresentation> putUserInGroup(final String realm, final String token,
      final String keycloakClientId, final String userName, final String groupName) {
    Uni<String> userId = this.getUserInfo(realm, token, keycloakClientId, userName)
        .map(KeycloakUserRepresentation::getId);

    Uni<String> groupId = this.getGroupInfo(realm, token, keycloakClientId, groupName)
        .map(GroupRepresentation::getId);

    Uni<Tuple2<String, String>> combinedUniTuple = Uni.combine().all().unis(userId, groupId)
        .asTuple();

    return combinedUniTuple.flatMap(
            tuple2 -> keycloakClient.putUserInGroup(BEARER + token, realm, GRANT_TYPE,
                keycloakClientId, tuple2.getItem1(), tuple2.getItem2()))
        .replaceWith(this.getUserInfo(realm, token, keycloakClientId, userName));
  }

  /**
   * Removes a user from a group. It can throw NoSuchGroupException or NoSuchUserException
   * exceptions.
   *
   * @param userName  name of the user that is going to be removed.
   * @param groupName name of the group.
   * @return a UserRepresentation of the user that has been kicked from the group.
   */
  public Uni<KeycloakUserRepresentation> deleteUserFromGroup(final String realm, final String token,
      final String keycloakClientId, final String userName, final String groupName) {
    Uni<String> userId = this.getUserInfo(realm, token, keycloakClientId, userName).onItem()
        .transform(userInfo -> userInfo.id);

    Uni<String> groupId = this.getGroupInfo(realm, token, keycloakClientId, groupName)
        .map(GroupRepresentation::getId);

    Uni<Tuple2<String, String>> combinedUniTuple = Uni.combine().all().unis(userId, groupId)
        .asTuple();

    return combinedUniTuple.flatMap(
            tuple2 -> keycloakClient.deleteUserFromGroup(BEARER + token, realm, GRANT_TYPE,
                keycloakClientId, tuple2.getItem1(), tuple2.getItem2()))
        .replaceWith(this.getUserInfo(realm, token, keycloakClientId, userName));
  }

  /**
   * Get all the users that has the given role assigned (but not effective)
   *
   * @param role role name used to query the users
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
   * @param role role name used to query the groups
   * @return List of GroupRepresentation with the desired groups
   */
  public Uni<List<GroupRepresentation>> getAllGroupsInAssignedRole(final String realm,
      final String token, final String keycloakClientId, final String role) {

    return keycloakClient.getAllGroupsInRole(BEARER + token, realm, GRANT_TYPE, keycloakClientId,
        role).map(GroupRepresentation::allFrom);
  }

  /**
   * Get all the users that has the given role effective, in order to do that, we get all the users
   * which have the given role assigned, and query all the groups that has the role assigned,
   * subsequently, it gets all the users that belongs to the groups and merge all in a set
   *
   * @param roleName role name used to query the groups
   * @return Set of KeycloakUserRepresentation with the desired users.
   */
  public Uni<Set<KeycloakUserRepresentation>> getAllUserInEffectiveRole(final String realm,
      final String token, final String keycloakClientId, final String roleName) {
    Uni<List<KeycloakUserRepresentation>> userAssigned = this.getAllUsersInAssignedRole(realm,
        token, keycloakClientId, roleName);
    Uni<List<GroupRepresentation>> groupAssigned = this.getAllGroupsInAssignedRole(realm, token,
        keycloakClientId, roleName);
    return Uni.combine().all().unis(userAssigned, groupAssigned)
        .combinedWith((users, groups) -> {
          Builder<List<KeycloakUserRepresentation>> builder = Uni.join().builder();
          for (GroupRepresentation group : groups) {
            builder.add(this.getUsersForGroup(realm, token, keycloakClientId, group.name));
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
