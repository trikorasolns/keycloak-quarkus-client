package com.trikorasolutions.keycloak.client.bl;

import com.trikorasolutions.keycloak.client.clientresource.KeycloakAuthAdminResource;
import com.trikorasolutions.keycloak.client.dto.UserRepresentation;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonArray;

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

  @RestClient
  KeycloakAuthAdminResource keycloakClient;

  /**
   * Creates a new user in the Keycloak database.
   *
   * @param newUser a UserRepresentation of the user that is going to be created.
   * @return A JsonArray with the UserRepresentation of the created user.
   */
  public Uni<JsonArray> createUser(final String realm, final String token, final String keycloakClientId, final UserRepresentation newUser) {
    return keycloakClient.createUser("Bearer " + token, realm, "implicit", keycloakClientId, newUser);
  }

  /**
   * Updated a user in Keycloak.
   *
   * @param userId  Id of the user that is going to be updated.
   * @param newUser Raw string containing the new user data in the UserRepresentation format.
   * @return A UserRepresentation of the updated user.
   */
  public Uni<JsonArray> updateUser(final String realm, final String token, final String keycloakClientId, final String userId, final UserRepresentation newUser) {
    return keycloakClient.updateUser("Bearer " + token, realm, "implicit", keycloakClientId, userId, newUser);
  }

  /**
   * Return the UserRepresentation of one user queried by his username.
   *
   * @param userName username of the user witch is going to be searched.
   * @return a UserRepresentation of the user.
   */
  public Uni<JsonArray> getUserInfo(final String realm, final String token, final String keycloakClientId, final String userName) {
    return keycloakClient.getUserInfo("Bearer " + token, realm, "implicit", keycloakClientId, userName);
  }

  /**
   * Deletes a user from the Keycloak database.
   *
   * @param id id of the user that is going to be deleted from the keycloak database.
   * @return an empty JsonArray.
   */
  public Uni<JsonArray> deleteUser(final String realm, final String token, final String keycloakClientId, final String id) {
    return keycloakClient.deleteUser("Bearer " + token, realm, "implicit", keycloakClientId, id);
  }

  /**
   * This method return a list with all the user in the client provided as argument
   *
   * @return a JsonArray of Keycloak UserRepresentations.
   */
  public Uni<JsonArray> listAll(final String realm, final String token, final String keycloakClientId) {
    return keycloakClient.listAll("Bearer " + token, realm, "implicit", keycloakClientId);
  }

  /**
   * Return information of one group.
   *
   * @param groupName name of the group that is going to be queried in the Keycloak database.
   * @return a GroupRepresentation of the desired group.
   */
  public Uni<JsonArray> getGroupInfo(final String realm, final String token, final String keycloakClientId, final String groupName) {
    return keycloakClient.getGroupInfo("Bearer " + token, realm, "implicit", keycloakClientId, groupName);
  }

  /**
   * Gets all the users that belongs to a concrete group.
   *
   * @param groupId id of the group that is going to be queried.
   * @return a JsonArray of UserRepresentation.
   */
  public Uni<JsonArray> getUsersForGroup(final String realm, final String token, final String keycloakClientId, final String groupId) {
    return keycloakClient.getGroupUsers("Bearer " + token, realm, "implicit", keycloakClientId, groupId);
  }

  /**
   * Add a user to a group.
   *
   * @param userId  id of the user that is going to be added.
   * @param groupId id of the group where the user will belong to.
   * @return an empty JsonArray.
   */
  public Uni<JsonArray> putUserInGroup(final String realm, final String token, final String keycloakClientId, final String userId, final String groupId) {
    return keycloakClient.putUserInGroup("Bearer " + token, realm, "implicit", keycloakClientId, userId, groupId);
  }

  /**
   * Removes a user from a group.
   *
   * @param userId  id of the user that is going to be removed.
   * @param groupId id of the group.
   * @return an empty JsonArray.
   */
  public Uni<JsonArray> deleteUserFromGroup(final String realm, final String token, final String keycloakClientId, final String userId, final String groupId) {
    return keycloakClient.deleteUserFromGroup("Bearer " + token, realm, "implicit", keycloakClientId, userId, groupId);
  }
}