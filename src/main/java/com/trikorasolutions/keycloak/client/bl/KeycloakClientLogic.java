package com.trikorasolutions.keycloak.client.bl;

import com.trikorasolutions.keycloak.client.clientresource.KeycloakAuthAdminResource;
import com.trikorasolutions.keycloak.client.dto.UserRepresentation;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonArray;
import java.util.NoSuchElementException;

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
   * @return a UserRepresentation of the created user.
   */
  public Uni<JsonArray> createUser(final String realm, final String token, final String keycloakClientId, final UserRepresentation newUser) {
    return keycloakClient.createUser("Bearer " + token, realm, "implicit", keycloakClientId, newUser)
      .onFailure().transform((x) -> new IllegalArgumentException())
      .replaceWith(keycloakClient.getUserInfo("Bearer " + token, realm, "implicit", keycloakClientId, newUser.username));
  }

  /**
   * Updated a user in Keycloak.
   *
   * @param userName  username of the user that is going to be updated.
   * @param newUser raw string containing the new user data in the UserRepresentation format.
   * @return a UserRepresentation of the updated user.
   */
  public Uni<JsonArray> updateUser(final String realm, final String token, final String keycloakClientId, final String userName, final UserRepresentation newUser) {
    return keycloakClient.getUserInfo("Bearer " + token, realm, "implicit", keycloakClientId, userName)
      .onItem().transform(jsonArray-> jsonArray.isEmpty() ? null : jsonArray.get(0).asJsonObject())
      .onItem().ifNull().failWith(() ->new NoSuchElementException()).onItem().ifNotNull()
      .transform(userInfo -> userInfo.getString("id"))
      .call(userId-> keycloakClient.updateUser("Bearer " + token, realm, "implicit", keycloakClientId, userId, newUser))
      .replaceWith(keycloakClient.getUserInfo("Bearer " + token, realm, "implicit", keycloakClientId, userName));
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
   * @param userName name of the user that is going to be deleted from the keycloak database.
   * @return a UserRepresentation of the deleted user.
   */
  public Uni<JsonArray> deleteUser(final String realm, final String token, final String keycloakClientId, final String userName) {
    return keycloakClient.getUserInfo("Bearer " + token, realm, "implicit", keycloakClientId, userName)
      .onItem().transform(jsonArray-> jsonArray.isEmpty() ? null : jsonArray.get(0).asJsonObject())
      .onItem().ifNull().failWith(() ->new NoSuchElementException()).onItem().ifNotNull()
      .transform(userInfo -> userInfo.getString("id"))
      .call(userId-> keycloakClient.deleteUser("Bearer " + token, realm, "implicit", keycloakClientId, userId))
      .replaceWith(keycloakClient.getUserInfo("Bearer " + token, realm, "implicit", keycloakClientId, userName));
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
   * @param groupName name of the group that is going to be queried.
   * @return a JsonArray of UserRepresentation.
   */
  public Uni<JsonArray> getUsersForGroup(final String realm, final String token, final String keycloakClientId, final String groupName) {
    return keycloakClient.getGroupInfo("Bearer " + token, realm, "implicit", keycloakClientId, groupName)
      .onItem().transform(jsonArray-> jsonArray.isEmpty() ? null : jsonArray.get(0).asJsonObject())
      .onItem().ifNull().failWith(() ->new NoSuchElementException()).onItem().ifNotNull()
      .transform(userInfo -> userInfo.getString("id"))
      .onItem().transformToUni(userId-> keycloakClient.getGroupUsers("Bearer " + token, realm, "implicit", keycloakClientId, userId));
  }

  /**
   * Add a user to a group.
   *
   * @param userName  name of the user that is going to be added.
   * @param groupName name of the group where the user will belong to.
   * @return a UserRepresentation of the user that has been added to the group.
   */
  public Uni<JsonArray> putUserInGroup(final String realm, final String token, final String keycloakClientId, final String userName, final String groupName) {
    Uni<String> userId = keycloakClient.getUserInfo("Bearer " + token, realm, "implicit", keycloakClientId, userName)
      .onItem().transform(jsonArray-> jsonArray.isEmpty() ? null : jsonArray.get(0).asJsonObject())
      .onItem().ifNull().failWith(() ->new NoSuchElementException()).onItem().ifNotNull()
      .transform(userInfo -> userInfo.getString("id"));

    Uni<String> groupId = keycloakClient.getGroupInfo("Bearer " + token, realm, "implicit", keycloakClientId, groupName)
      .onItem().transform(jsonArray-> jsonArray.isEmpty() ? null : jsonArray.get(0).asJsonObject())
      .onItem().ifNull().failWith(() ->new NoSuchElementException()).onItem().ifNotNull()
      .transform(userInfo -> userInfo.getString("id"));

    Uni<Tuple2<String, String>> combinedUniTuple = Uni.combine().all().unis(userId, groupId).asTuple();

    return combinedUniTuple.onItem()
      .transformToUni(tuple2 ->keycloakClient.putUserInGroup("Bearer " + token, realm, "implicit", keycloakClientId,
        tuple2.getItem1(), tuple2.getItem2()))
      .replaceWith(keycloakClient.getUserInfo("Bearer " + token, realm, "implicit", keycloakClientId, userName));

  }

  /**
   * Removes a user from a group.
   *
   * @param userName  name of the user that is going to be removed.
   * @param groupName name of the group.
   * @return a UserRepresentation of the user that has been kicked from the group.
   */
  public Uni<JsonArray> deleteUserFromGroup(final String realm, final String token, final String keycloakClientId, final String userName, final String groupName) {
    Uni<String> userId = keycloakClient.getUserInfo("Bearer " + token, realm, "implicit", keycloakClientId, userName)
      .onItem().transform(jsonArray-> jsonArray.isEmpty() ? null : jsonArray.get(0).asJsonObject())
      .onItem().ifNull().failWith(() ->new NoSuchElementException()).onItem().ifNotNull()
      .transform(userInfo -> userInfo.getString("id"));

    Uni<String> groupId = keycloakClient.getGroupInfo("Bearer " + token, realm, "implicit", keycloakClientId, groupName)
      .onItem().transform(jsonArray-> jsonArray.isEmpty() ? null : jsonArray.get(0).asJsonObject())
      .onItem().ifNull().failWith(() ->new NoSuchElementException()).onItem().ifNotNull()
      .transform(userInfo -> userInfo.getString("id"));

    Uni<Tuple2<String, String>> combinedUniTuple = Uni.combine().all().unis(userId, groupId).asTuple();

    return combinedUniTuple.onItem()
      .transformToUni(tuple2 ->keycloakClient.deleteUserFromGroup("Bearer " + token, realm, "implicit", keycloakClientId,
        tuple2.getItem1(), tuple2.getItem2()))
      .replaceWith(keycloakClient.getUserInfo("Bearer " + token, realm, "implicit", keycloakClientId, userName));  }
}
