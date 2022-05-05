package com.trikorasolutions.keycloak.client.clientresource;

import com.trikorasolutions.keycloak.client.dto.UserRepresentation;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import javax.json.JsonArray;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Common arguments to all the methods:
 * <ul>
 * <li> bearerToken access token provided by the  keycloak SecurityIdentity.
 * <li> realm the realm name in which the users are going to be queried.
 * <li> grantType kind of authentication method.
 * <li> clientId id of the client (service name).
 * </ul>
 */
@Path("/auth/admin")
@RegisterRestClient(configKey = "keycloak-api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface KeycloakAuthAdminResource {

  /**
   * Register a new user in the Keycloak client.
   *
   * @param body raw string containing the new user in the UserRepresentation format.
   * @return -
   */
  @POST
  @Path("/realms/{realm}/users")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonArray> createUser(@HeaderParam("Authorization") String bearerToken,
      @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId, UserRepresentation body);

  /**
   * Updated a user in Keycloak.
   *
   * @param id   Id of the user that is going to be updated.
   * @param body Raw string containing the new user data in the UserRepresentation format.
   * @return A UserRepresentation of the updated user.
   */
  @PUT
  @Path("/realms/{realm}/users/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonArray> updateUser(@HeaderParam("Authorization") String bearerToken,
      @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId, @PathParam("id") String id,
      UserRepresentation body);

  /**
   * Return the UserRepresentation of one user queried by his username.
   *
   *
   * <b>NOTE: </b> if the username == null, then Keycloak return a JsonArray with all
   * the users in the realm.
   *
   * @param username username of the user witch is going to be searched.
   * @return a UserRepresentation of the user.
   */
  @GET
  @Path("/realms/{realm}/users")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonArray> getUserInfo(@HeaderParam("Authorization") String bearerToken,
      @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId, @QueryParam("username") String username);

  /**
   * Deletes a user from the Keycloak database.
   *
   * @param id id of the user that is going to be deleted from the keycloak database.
   * @return a response with body equals to: "success".
   */
  @DELETE
  @Path("/realms/{realm}/users/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonArray> deleteUser(@HeaderParam("Authorization") String bearerToken,
      @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId, @PathParam("id") String id);

  /**
   * This method return a list with all the user in the client provided as argument
   *
   * @return a JsonArray of Keycloak UserRepresentations.
   */
  @GET
  @Path("/realms/{realm}/users")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonArray> listAllUsers(@HeaderParam("Authorization") String bearerToken,
      @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId);

  /**
   * This method return a list with all the groups in the client provided as argument
   *
   * @return a JsonArray of Keycloak GroupRepresentations.
   */
  @GET
  @Path("/realms/{realm}/groups")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonArray> listAllGroups(@HeaderParam("Authorization") String bearerToken,
      @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId);

  /**
   * Return information of one group.
   *
   * @param groupName name of the group that is going to be queried in the Keycloak database.
   * @return a GroupRepresentation of the desired group.
   */
  @GET
  @Path("/realms/{realm}/groups")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonArray> getGroupInfo(@HeaderParam("Authorization") String bearerToken,
      @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId, @QueryParam("search") String groupName);

  /**
   * Gets all the users that belongs to a concrete group.
   *
   * @param id id of the group that is going to be queried.
   * @return a JsonArray of UserRepresentation.
   */
  @GET
  @Path("/realms/{realm}/groups/{id}/members")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonArray> getGroupUsers(@HeaderParam("Authorization") String bearerToken,
      @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId, @PathParam("id") String id);

  /**
   * Add a user to a group.
   *
   * @param id      id of the user that is going to be added.
   * @param groupId id of the group where the user will belong to.
   * @return an empty JsonArray.
   */
  @PUT
  @Path("/realms/{realm}/users/{id}/groups/{groupId}")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonArray> putUserInGroup(@HeaderParam("Authorization") String bearerToken,
      @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId, @PathParam("id") String id,
      @PathParam("groupId") String groupId);

  /**
   * Removes a user from a group.
   *
   * @param id      id of the user that is going to be removed.
   * @param groupId id of the group.
   * @return an empty JsonArray.
   */
  @DELETE
  @Path("/realms/{realm}/users/{id}/groups/{groupId}")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonArray> deleteUserFromGroup(@HeaderParam("Authorization") String bearerToken,
      @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId, @PathParam("id") String id,
      @PathParam("groupId") String groupId);

  /**
   * Return the users which have ASSIGNED the given role
   * @param roleName role name
   * @return JsonArray with all the users
   */
  @GET
  @Path("/realms/{realm}/roles/{role-name}/users")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonArray> getAllUsersInRole(@HeaderParam("Authorization") String bearerToken,
      @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId, @PathParam("role-name") String roleName);

  /**
   * Return the groups which have ASSIGNED the given role
   * @param roleName role name
   * @return JsonArray with all the groups
   */
  @GET
  @Path("/realms/{realm}/roles/{role-name}/groups")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonArray> getAllGroupsInRole(@HeaderParam("Authorization") String bearerToken,
      @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId, @PathParam("role-name") String roleName);

  /**
   * Return ALL the roles of one user
   * @param userId id of the user to be queried
   * @return JsonArray with the roles
   */
  @GET
  @Path("/realms/{realm}/users/{id}/role-mappings/realm/composite")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonArray> getUserRoles(@HeaderParam("Authorization") String bearerToken,
      @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId, @PathParam("id") String userId);

  /**
   * Return ALL the roles of one group
   * @param groupId id of the user to be queried
   * @return JsonArray with the roles
   */
  @GET
  @Path("/realms/{realm}/groups/{id}/role-mappings/realm/composite")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonArray> getGroupRoles(@HeaderParam("Authorization") String bearerToken,
      @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId, @PathParam("id") String groupId);
}
