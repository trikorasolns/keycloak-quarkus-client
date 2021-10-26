package com.trikorasolutions.keycloak.client.bl;

import com.trikorasolutions.keycloak.client.clientresource.KeycloakAuthAdminResource;
import com.trikorasolutions.keycloak.client.dto.KeycloakUserRepresentation;
import com.trikorasolutions.keycloak.client.dto.UserRepresentation;
import com.trikorasolutions.keycloak.client.exception.ArgumentsFormatException;
import com.trikorasolutions.keycloak.client.exception.NoSuchGroupException;
import com.trikorasolutions.keycloak.client.exception.NoSuchUserException;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.unchecked.Unchecked;
import static io.smallrye.mutiny.unchecked.Unchecked.*;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonArray;
import javax.json.JsonValue;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

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
  public Uni<KeycloakUserRepresentation> createUser(final String realm, final String token, final String keycloakClientId, final UserRepresentation newUser)
    throws ArgumentsFormatException, NoSuchUserException {
    return keycloakClient.createUser("Bearer " + token, realm, "implicit", keycloakClientId, newUser)
      //.onFailure().transform(Unchecked.function(x ->{ throw new ArgumentsFormatException();}))
      .onFailure().transform(x->new ArgumentsFormatException())
      //.onFailure().invoke(Unchecked.consumer(x ->{throw new ArgumentsFormatException();}))
      //.onFailure(org.jboss.resteasy.reactive.ClientWebApplicationException.class).transform(throwable ->  new ArgumentsFormatException())
      //.onFailure(org.jboss.resteasy.reactive.ClientWebApplicationException.class).transform(x->null).onItem().ifNull().failWith(()-> new ArgumentsFormatException())
      //.onFailure().transform((x)-> new ArgumentsFormatException())
      .replaceWith(this.getUserInfo(realm, token, keycloakClientId, newUser.username));
  }

  /**
   * Updated a user in Keycloak.
   *
   * @param userName  username of the user that is going to be updated.
   * @param newUser raw string containing the new user data in the UserRepresentation format.
   * @return a UserRepresentation of the updated user.
   */
  public Uni<KeycloakUserRepresentation> updateUser(final String realm, final String token, final String keycloakClientId, final String userName, final UserRepresentation newUser) throws NoSuchUserException {
    return keycloakClient.getUserInfo("Bearer " + token, realm, "implicit", keycloakClientId, userName)
      .onItem().transform(jsonArray-> jsonArray.isEmpty() ? null : jsonArray.get(0).asJsonObject())
      .onItem().ifNull().failWith(() ->new NoSuchUserException()).onItem().ifNotNull()
      .transform(userInfo -> userInfo.getString("id"))
      .call(userId-> keycloakClient.updateUser("Bearer " + token, realm, "implicit", keycloakClientId, userId, newUser))
      .replaceWith(this.getUserInfo(realm, token, keycloakClientId, newUser.username));
  }

  /**
   * Return the UserRepresentation of one user queried by his username.
   *
   * @param userName username of the user witch is going to be searched.
   * @return a UserRepresentation of the user.
   */
  public Uni<KeycloakUserRepresentation> getUserInfo(final String realm, final String token, final String keycloakClientId, final String userName){
    return keycloakClient.getUserInfo("Bearer " + token, realm, "implicit", keycloakClientId, userName)
      .onItem().transform(jsonArray-> jsonArray.isEmpty() ? null : jsonArray.get(0).asJsonObject())
      .onItem().ifNull().failWith(() ->new NoSuchUserException())
      .onItem().ifNotNull().transform(KeycloakUserRepresentation::from);
  }

  /**
   * Deletes a user from the Keycloak database.
   *
   * @param userName name of the user that is going to be deleted from the keycloak database.
   * @return a UserRepresentation of the deleted user.
   */
  public Uni<KeycloakUserRepresentation> deleteUser(final String realm, final String token, final String keycloakClientId, final String userName) {
    return this.getUserInfo(realm, token, keycloakClientId, userName)
      .onItem().transform(userInfo -> userInfo.id)
      .call(userId-> keycloakClient.deleteUser("Bearer " + token, realm, "implicit", keycloakClientId, userId))
      .replaceWith(keycloakClient.getUserInfo("Bearer " + token, realm, "implicit", keycloakClientId, userName).onItem()
        .transform(KeycloakUserRepresentation::from));
  }

  /**
   * This method return a list with all the user in the client provided as argument
   *
   * @return a JsonArray of Keycloak UserRepresentations.
   */
  public Uni<List<KeycloakUserRepresentation>> listAll(final String realm, final String token, final String keycloakClientId) {
    return keycloakClient.listAll("Bearer " + token, realm, "implicit", keycloakClientId)
      .onItem().transform(userList -> userList.stream()
      .map(JsonValue::asJsonObject)
      .map(KeycloakUserRepresentation::from)
      .collect(Collectors.toList()));
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
  public Uni<List<KeycloakUserRepresentation>> getUsersForGroup(final String realm, final String token, final String keycloakClientId, final String groupName) {
    return keycloakClient.getGroupInfo("Bearer " + token, realm, "implicit", keycloakClientId, groupName)
      .onItem().transform(jsonArray-> jsonArray.isEmpty() ? null : jsonArray.get(0).asJsonObject())
      .onItem().ifNull().failWith(() ->new NoSuchGroupException()).onItem().ifNotNull()
      .transform(userInfo -> userInfo.getString("id"))
      .onItem().transformToUni(userId-> keycloakClient.getGroupUsers("Bearer " + token, realm, "implicit", keycloakClientId, userId))
      .onItem().transform(userList -> userList.stream()
        .map(JsonValue::asJsonObject)
        .map(KeycloakUserRepresentation::from)
        .collect(Collectors.toList()));
  }

  /**
   * Add a user to a group.
   *
   * @param userName  name of the user that is going to be added.
   * @param groupName name of the group where the user will belong to.
   * @return a UserRepresentation of the user that has been added to the group.
   */
  public Uni<KeycloakUserRepresentation> putUserInGroup(final String realm, final String token, final String keycloakClientId, final String userName, final String groupName) throws NoSuchUserException {
    Uni<String> userId = keycloakClient.getUserInfo("Bearer " + token, realm, "implicit", keycloakClientId, userName)
      .onItem().transform(jsonArray-> jsonArray.isEmpty() ? null : jsonArray.get(0).asJsonObject())
      .onItem().ifNull().failWith(() ->new NoSuchUserException()).onItem().ifNotNull()
      .transform(userInfo -> userInfo.getString("id"));

    Uni<String> groupId = keycloakClient.getGroupInfo("Bearer " + token, realm, "implicit", keycloakClientId, groupName)
      .onItem().transform(jsonArray-> jsonArray.isEmpty() ? null : jsonArray.get(0).asJsonObject())
      .onItem().ifNull().failWith(() ->new NoSuchGroupException()).onItem().ifNotNull()
      .transform(userInfo -> userInfo.getString("id"));

    Uni<Tuple2<String, String>> combinedUniTuple = Uni.combine().all().unis(userId, groupId).asTuple();

    return combinedUniTuple.onItem()
      .transformToUni(tuple2 ->keycloakClient.putUserInGroup("Bearer " + token, realm, "implicit", keycloakClientId,
        tuple2.getItem1(), tuple2.getItem2()))
      .replaceWith(this.getUserInfo(realm, token, keycloakClientId, userName));
  }

  /**
   * Removes a user from a group.
   *
   * @param userName  name of the user that is going to be removed.
   * @param groupName name of the group.
   * @return a UserRepresentation of the user that has been kicked from the group.
   */
  public Uni<KeycloakUserRepresentation> deleteUserFromGroup(final String realm, final String token, final String keycloakClientId, final String userName, final String groupName)
    throws NoSuchUserException{
    Uni<String> userId = keycloakClient.getUserInfo("Bearer " + token, realm, "implicit", keycloakClientId, userName)
      .onItem().transform(jsonArray-> jsonArray.isEmpty() ? null : jsonArray.get(0).asJsonObject())
      .onItem().ifNull().failWith(() ->new NoSuchUserException()).onItem().ifNotNull()
      .transform(userInfo -> userInfo.getString("id"));

    Uni<String> groupId = keycloakClient.getGroupInfo("Bearer " + token, realm, "implicit", keycloakClientId, groupName)
      .onItem().transform(jsonArray-> jsonArray.isEmpty() ? null : jsonArray.get(0).asJsonObject())
      .onItem().ifNull().failWith(() ->new NoSuchGroupException()).onItem().ifNotNull()
      .transform(userInfo -> userInfo.getString("id"));

    Uni<Tuple2<String, String>> combinedUniTuple = Uni.combine().all().unis(userId, groupId).asTuple();

    return combinedUniTuple.onItem()
      .transformToUni(tuple2 ->keycloakClient.deleteUserFromGroup("Bearer " + token, realm, "implicit", keycloakClientId,
        tuple2.getItem1(), tuple2.getItem2()))
      .replaceWith(this.getUserInfo(realm, token, keycloakClientId, userName));
  }
}
