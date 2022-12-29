package com.trikorasolutions.keycloak.client.bl;

import static com.trikorasolutions.keycloak.client.bl.KeycloakClientLogic.BEARER;
import static com.trikorasolutions.keycloak.client.bl.KeycloakClientLogic.GRANT_TYPE;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

import com.trikorasolutions.keycloak.client.clientresource.KeycloakAuthAdminResource;
import com.trikorasolutions.keycloak.client.clientresource.KeycloakAuthorizationResource;
import com.trikorasolutions.keycloak.client.dto.KeycloakUserRepresentation;
import com.trikorasolutions.keycloak.client.dto.RoleRepresentation;
import com.trikorasolutions.keycloak.client.dto.TrikoraGroupRepresentation;
import com.trikorasolutions.keycloak.client.exception.ArgumentsFormatException;
import com.trikorasolutions.keycloak.client.exception.ClientNotFoundException;
import com.trikorasolutions.keycloak.client.exception.DuplicatedGroupException;
import com.trikorasolutions.keycloak.client.exception.InvalidTokenException;
import com.trikorasolutions.keycloak.client.exception.NoSuchGroupException;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniJoin.Builder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.keycloak.representations.idm.GroupRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ApplicationScoped
public final class KeycloakGroupLogic {

  private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakGroupLogic.class);

  @ConfigProperty(name = "trikora.keycloak.buffer-size")
  private Integer KC_BUFFER_SIZE;

  @RestClient
  private KeycloakAuthAdminResource keycloakClient;

  @RestClient
  private KeycloakAuthorizationResource keycloakUserClient;

  @Inject
  private KeycloakClientLogic blClient;

  /******************************* GROUP FUNCTIONS *******************************/
  /**
   * This method return a list with all the groups in the client provided as argument
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @return a JsonArray of Keycloak UserRepresentations.
   */
  public Uni<List<TrikoraGroupRepresentation>> listAllGroups(final String realm, final String token,
      final String keycloakClientId) {
    return keycloakClient.listAllGroups(BEARER + token, realm, GRANT_TYPE, keycloakClientId)
        .map(TrikoraGroupRepresentation::allFrom);
  }

  /**
   * Creates a group in Keycloak
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param name             group that is going to be created into the Keycloak database, you can
   *                         create one dto with just the group name.
   * @param attributes       attributes that going to be mapped to the new group
   * @return a GroupRepresentation of the new group.
   */

  public Uni<TrikoraGroupRepresentation> createGroup(final String realm, final String token,
      final String keycloakClientId, final String name,
      final Map<String, List<String>> attributes) {

    return keycloakClient.createGroup(BEARER + token, realm, GRANT_TYPE, keycloakClientId,
            this.createUploadDto(name, attributes))
        .onFailure(ClientWebApplicationException.class).transform(ex -> {
          if (ex.getMessage().contains(String.valueOf(CONFLICT.getStatusCode()))) {
            return new DuplicatedGroupException(name);
          } else if (ex.getMessage().contains(String.valueOf(UNAUTHORIZED.getStatusCode()))) {
            return new InvalidTokenException();
          } else if (ex.getMessage().contains(String.valueOf(NOT_FOUND.getStatusCode()))) {
            return new ClientNotFoundException(keycloakClientId, realm);
          } else {
            return new ArgumentsFormatException(
                "The group representation provided to Keycloak is incorrect, with error: "
                    + ex.getMessage());
          }
        })
        .replaceWith(this.getGroupInfoNoEnrich(realm, token, keycloakClientId, name));
  }

  /**
   * Return information of one group. And enrich it with its members. It can throw
   * NoSuchGroupException.
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param groupName        name of the group that is going to be queried in the Keycloak
   *                         database.
   * @return a GroupRepresentation of the desired group.
   */
  public Uni<TrikoraGroupRepresentation> getGroupInfo(final String realm, final String token,
      final String keycloakClientId, final String groupName) {
    return getGroupInfoNoEnrich(realm, token, keycloakClientId, groupName)
        .flatMap(group -> this.getGroupRolesById(realm, token, keycloakClientId, group.getId())
            .map(group::addRoles))
        .flatMap(group -> this.getGroupMembers(realm, token, keycloakClientId, group.getName())
            .map(group::addMembers)
        );
  }

  public Uni<TrikoraGroupRepresentation> getGroupInfoNoEnrich(final String realm,
      final String token,
      final String keycloakClientId, final String groupName) {
    return keycloakClient.getGroupInfo(BEARER + token, realm, GRANT_TYPE, keycloakClientId,
            groupName, Boolean.TRUE)
        .map(jsonArray -> (jsonArray.size() != 1) ? null : jsonArray.get(0).asJsonObject())
        .onItem().ifNull().failWith(() -> new NoSuchGroupException(groupName))
        .map(TrikoraGroupRepresentation::from);
  }

  /**
   * Updates a group in Keycloak. You can only update the attributes with this function. If you need
   * to add realmRoles, you can use {@link #addRolesToGroup(String, String, String, String,
   * String...) addRolesToGroup}. In a similar way, you can use {@link #removeRolesFromGroup(String,
   * String, String, String, String...) removeRolesToGroup } to remove some roles to the given
   * group
   *
   * @param realm            the realm groupName in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service groupName).
   * @param groupName        groupName of the group that is desired to be updated.
   * @param attributes       attributes mapping for the group
   * @return True if the groups has been removed from the DB, False otherwise.
   */
  public Uni<TrikoraGroupRepresentation> updateGroup(final String realm, final String token,
      final String keycloakClientId, final String groupName,
      final Map<String, List<String>> attributes) {
    return this.getGroupInfoNoEnrich(realm, token, keycloakClientId, groupName)
        .map(TrikoraGroupRepresentation::getId)
        .call(groupId -> keycloakClient.updateGroup(BEARER + token, realm, GRANT_TYPE,
            keycloakClientId, groupId,
            this.createUploadDto(groupName, attributes)))
        .replaceWith(this.getGroupInfoNoEnrich(realm, token, keycloakClientId, groupName));
  }

  /**
   * Deletes a group in Keycloak
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param groupName        name of the group that is desired to be deleted.
   * @return True if the groups has been removed from the DB, False otherwise.
   */
  public Uni<Boolean> deleteGroup(final String realm, final String token,
      final String keycloakClientId, final String groupName) {
    return this.getGroupInfoNoEnrich(realm, token, keycloakClientId, groupName)
        .map(TrikoraGroupRepresentation::getId)
        .flatMap(groupId -> keycloakClient.deleteGroup(BEARER + token, realm, GRANT_TYPE,
            keycloakClientId, groupId))
        .map(x -> Boolean.TRUE)
        .onFailure(NoSuchGroupException.class).recoverWithItem(Boolean.FALSE);
  }

  private Uni<String> generateRolesJsonFromNameArray(final String realm, final String token,
      final String keycloakClientId, String... roles) {
    Builder<String> builder = Uni.join().builder();
    LinkedHashMap<String, String> idMapper = new LinkedHashMap<>();
    Arrays.stream(roles).forEach(roleName ->
        builder.add(blClient.getRoleInfoNoEnrich(realm, token, keycloakClientId, roleName)
            .map(role -> idMapper.put(role.name, role.id))
        )
    );
    return builder.joinAll().andCollectFailures()
        .map(ids -> {
          StringJoiner newRoles = new StringJoiner(",", "[", "]");
          for (String role : idMapper.keySet()) {
            newRoles.add("{\"id\": \"" + idMapper.get(role) + "\", \"name\": \"" + role + "\"}");
          }
          LOGGER.debug("Roles to add/delete: {}", newRoles);
          return newRoles.toString();
        });

  }

  /**
   * Add the given roles to the given group in Keycloak.
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param groupName        name of the group that is desired to be updated.
   * @param roles            an array  of the roles that are going to be added to the group.
   * @return An enriched GroupRepresentation, containing all the roles of the group.
   */
  public Uni<TrikoraGroupRepresentation> addRolesToGroup(final String realm, final String token,
      final String keycloakClientId, final String groupName, String... roles) {

    return this.generateRolesJsonFromNameArray(realm, token, keycloakClientId, roles)
        .flatMap(rolesStr ->
            this.getGroupInfoNoEnrich(realm, token, keycloakClientId, groupName)
                .map(TrikoraGroupRepresentation::getId)
                .flatMap(
                    groupId -> keycloakClient.addRolesToGroup(BEARER + token, realm, GRANT_TYPE,
                        keycloakClientId, groupId, rolesStr))
                .replaceWith(this.getGroupInfo(realm, token, keycloakClientId, groupName))
        );
  }

  /**
   * Removes the given roles from the given group in Keycloak.
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param groupName        name of the group that is desired to be updated.
   * @param roles            an array of the roles that are going to be  removed from the group.
   * @return An enriched GroupRepresentation, containing all the roles of the group.
   */
  public Uni<TrikoraGroupRepresentation> removeRolesFromGroup(final String realm,
      final String token,
      final String keycloakClientId, final String groupName, String... roles) {

    return this.generateRolesJsonFromNameArray(realm, token, keycloakClientId, roles)
        .flatMap(rolesStr ->
            this.getGroupInfoNoEnrich(realm, token, keycloakClientId, groupName)
                .map(TrikoraGroupRepresentation::getId)
                .flatMap(
                    groupId -> keycloakClient.removeRolesToGroup(BEARER + token, realm, GRANT_TYPE,
                        keycloakClientId, groupId, rolesStr))
                .replaceWith(this.getGroupInfo(realm, token, keycloakClientId, groupName))

        );
  }

  /**
   * Returns all the users that belongs to a concrete group. It can throw NoSuchGroupException.
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param groupName        name of the group that is going to be queried.
   * @return a JsonArray of GroupRepresentation.
   */
  public Uni<List<KeycloakUserRepresentation>> getGroupMembers(final String realm,
      final String token, final String keycloakClientId, final String groupName) {
    return this.getGroupInfoNoEnrich(realm, token, keycloakClientId, groupName)
        .map(TrikoraGroupRepresentation::getId)
        .flatMap(groupId -> this.getGroupMembersRec(realm, token, keycloakClientId, groupId, 0,
            Integer.MAX_VALUE, new ArrayList<>()));

  }

  /**
   * Gets all the users that belongs to a concrete group. It can throw NoSuchGroupException.
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param groupName        name of the group that is going to be queried.
   * @param first            first user to be fetched within the members of the group
   * @param recCount         number of users to be fetched from the first one
   * @return a JsonArray of GroupRepresentation.
   */
  public Uni<List<KeycloakUserRepresentation>> getGroupMembers(final String realm,
      final String token, final String keycloakClientId, final String groupName, Integer first,
      Integer recCount) {
    return this.getGroupInfoNoEnrich(realm, token, keycloakClientId, groupName)
        .map(TrikoraGroupRepresentation::getId)
        .flatMap(groupId -> this.getGroupMembersRec(realm, token, keycloakClientId, groupId, first,
            recCount, new ArrayList<>()));

  }

  private Uni<List<KeycloakUserRepresentation>> getGroupMembersRec(final String realm,
      final String token,
      final String keycloakClientId, final String groupId, Integer first, Integer recCount,
      List<KeycloakUserRepresentation> res) {
    LOGGER.debug("#getGroupMembersRec(cursor, usersFetched)...{}-{}", first, res.size());
    return keycloakClient.getGroupUsers(BEARER + token, realm, GRANT_TYPE, keycloakClientId,
            groupId, first, (KC_BUFFER_SIZE < (recCount - first) ? KC_BUFFER_SIZE : recCount - first))
        .map(KeycloakUserRepresentation::allFrom)
        .flatMap(currentSelection -> {
          res.addAll(currentSelection);
          if (currentSelection.size() < KC_BUFFER_SIZE || res.size() >= recCount) {
            return Uni.createFrom().item(res); // Recursion Base case
          } else {
            return this.getGroupMembersRec(realm, token, keycloakClientId, groupId,
                first + KC_BUFFER_SIZE, recCount, res);
          }
        });
  }

  /**
   * Add a user to a group. It can throw NoSuchGroupException or NoSuchUserException exceptions.
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param userName         name of the user that is going to be added.
   * @param groupName        name of the group where the user will belong to.
   * @return a UserRepresentation of the user that has been added to the group.
   */
  public Uni<KeycloakUserRepresentation> putUserInGroup(final String realm, final String token,
      final String keycloakClientId, final String userName, final String groupName) {

    Uni<String> userId = blClient.getUserInfoNoEnrich(realm, token, keycloakClientId, userName)
        .map(KeycloakUserRepresentation::getId);

    Uni<String> groupId = this.getGroupInfoNoEnrich(realm, token, keycloakClientId, groupName)
        .map(TrikoraGroupRepresentation::getId);

    return Uni.combine().all().unis(userId, groupId).asTuple().flatMap(tuple2 ->
            keycloakClient.putUserInGroup(BEARER + token, realm, GRANT_TYPE,
                keycloakClientId, tuple2.getItem1(), tuple2.getItem2()))

        .flatMap(x -> blClient.getUserInfo(realm, token, keycloakClientId, userName));
  }

  /**
   * Removes a user from a group. It can throw NoSuchGroupException or NoSuchUserException
   * exceptions.
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param userName         name of the user that is going to be removed.
   * @param groupName        name of the group.
   * @return a UserRepresentation of the user that has been kicked from the group.
   */
  public Uni<KeycloakUserRepresentation> deleteUserFromGroup(final String realm, final String token,
      final String keycloakClientId, final String userName, final String groupName) {
    Uni<String> userId = blClient.getUserInfoNoEnrich(realm, token, keycloakClientId, userName)
        .map(KeycloakUserRepresentation::getId);

    Uni<String> groupId = this.getGroupInfoNoEnrich(realm, token, keycloakClientId, groupName)
        .map(TrikoraGroupRepresentation::getId);

    return Uni.combine().all().unis(userId, groupId).asTuple().flatMap(tuple2 ->
            keycloakClient.deleteUserFromGroup(BEARER + token, realm, GRANT_TYPE,
                keycloakClientId, tuple2.getItem1(), tuple2.getItem2()))
        .replaceWith(blClient.getUserInfo(realm, token, keycloakClientId, userName));
  }

  /**
   * Return the UserRepresentation of one user queried by his username. It can throw
   * NoSuchUserException.
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param groupName        username of the user witch is going to be searched.
   * @return a UserRepresentation of the user.
   */
  public Uni<List<RoleRepresentation>> getGroupRoles(final String realm, final String token,
      final String keycloakClientId, final String groupName) {
    return this.getGroupInfoNoEnrich(realm, token, keycloakClientId, groupName)
        .map(TrikoraGroupRepresentation::getId)
        .flatMap(id -> keycloakClient.getGroupRoles(BEARER + token, realm, GRANT_TYPE,
            keycloakClientId, id))
        .map(RoleRepresentation::allFrom);
  }

  /**
   * Return all the roles assigned to the given group.
   *
   * @param realm            the realm name in which the users are going to be queried.
   * @param token            access token provided by the keycloak SecurityIdentity.
   * @param keycloakClientId id of the client (service name).
   * @param id               the id of the group witch is going to be searched.
   * @return a UserRepresentation of the user.
   */
  public Uni<List<RoleRepresentation>> getGroupRolesById(final String realm, final String token,
      final String keycloakClientId, final String id) {
    return keycloakClient.getGroupRoles(BEARER + token, realm, GRANT_TYPE,
            keycloakClientId, id)
        .map(RoleRepresentation::allFrom);
  }

  private GroupRepresentation createUploadDto(final String name,
      final Map<String, List<String>> attributes) {
    GroupRepresentation representation = new GroupRepresentation();
    if (name != null && !name.isEmpty()) {
      representation.setName(name);
    }
    if (attributes != null && !attributes.isEmpty()) {
      representation.setAttributes(attributes);
    }
    return representation;
  }
}
