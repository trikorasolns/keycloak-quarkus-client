package com.trikorasolutions.keycloak.client.clientresource;

import com.trikorasolutions.keycloak.client.dto.RoleRepresentation;
import com.trikorasolutions.keycloak.client.dto.UserRepresentation;
import com.trikorasolutions.keycloak.client.dto.UserRepresentation.UserDtoCredential;
import io.smallrye.mutiny.Uni;
import javax.json.JsonArray;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Common arguments to all the methods:
 * <ul>
 * <li> bearerToken access token provided by the keycloak SecurityIdentity.
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
   * Updated a user password in Keycloak.
   *
   * @param id   Id of the user that is going to be updated.
   * @param body Raw string containing the new user password in the CredentialRepresentation
   *             format.
   * @return -.
   */
  @PUT
  @Path("/realms/{realm}/users/{id}/reset-password")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonArray> resetPassword(@HeaderParam("Authorization") String bearerToken,
      @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId, @PathParam("id") String id,
      UserDtoCredential body);

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
      @QueryParam("client_id") String clientId, @QueryParam("first") Integer first,
      @QueryParam("max") Integer max);

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
   * This will update the group and set the parent if it exists. Create it and set the parent if the
   * group doesn't exist.
   *
   * @param group that is going to be created in the Keycloak database.
   * @return a GroupRepresentation of the new group.
   */
  @POST
  @Path("/realms/{realm}/groups")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonArray> createGroup(@HeaderParam("Authorization") String bearerToken,
      @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId, String group);

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
   * This method deletes a group.
   *
   * @param id that is going to be deleted from the Keycloak database.
   * @return -
   */
  @DELETE
  @Path("/realms/{realm}/groups/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonArray> deleteGroup(@HeaderParam("Authorization") String bearerToken,
      @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId, @PathParam("id") String id);

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
      @QueryParam("client_id") String clientId, @PathParam("id") String id,
      @QueryParam("first") Integer first, @QueryParam("max") Integer max);

  /**
   * Return all the groups of a given user.
   *
   * @param id id of the user that is going to be added.
   * @return an empty JsonArray.
   */
  @GET
  @Path("/realms/{realm}/users/{id}/groups")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonArray> getUserGroups(@HeaderParam("Authorization") String bearerToken,
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
   *
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
   *
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
   *
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
   * Add the given role mappings to a group
   *
   * @param groupId id of the group to be upgraded.
   * @param roles   array containing the roles to be added, both id and name of the roles need to be
   *                provided.
   * @return -
   */
  @POST
  @Path("/realms/{realm}/groups/{id}/role-mappings/realm")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonArray> addRolesToGroup(@HeaderParam("Authorization") String bearerToken,
      @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId, @PathParam("id") String groupId,
      String roles);

  /**
   * Removes the given role mappings to a group
   *
   * @param groupId id of the group to be upgraded.
   * @param roles   array containing the roles to be added, both id and name of the roles need to be
   *                provided.
   * @return -
   */
  @DELETE
  @Path("/realms/{realm}/groups/{id}/role-mappings/realm")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonArray> removeRolesToGroup(@HeaderParam("Authorization") String bearerToken,
      @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId, @PathParam("id") String groupId,
      String roles);

  /**
   * Remove the given role mappings from a group
   *
   * @param groupId id of the group to be upgraded.
   * @param roles   array containing the roles to be added, both id and name of the roles need to be
   *                provided.
   * @return JsonArray with the roles
   */
  @DELETE
  @Path("/realms/{realm}/groups/{id}/role-mappings/realm")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonArray> removeRolesFromGroup(@HeaderParam("Authorization") String bearerToken,
      @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId, @PathParam("id") String groupId,
      RoleRepresentation[] roles);

  /**
   * Return ALL the roles of one group
   *
   * @param groupId id of the user to be queried
   * @return JsonArray with the roles
   */
  @GET
  @Path("/realms/{realm}/groups/{id}/role-mappings/realm/composite")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonArray> getGroupRoles(@HeaderParam("Authorization") String bearerToken,
      @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId, @PathParam("id") String groupId);

  /**
   * Return all the roles at realm level
   *
   * @return JsonArray with the role details
   */
  @GET
  @Path("/realms/{realm}/roles")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonArray> listRoles(@HeaderParam("Authorization") String bearerToken,
      @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId);

  /**
   * Creates a role
   *
   * @param role RoleRepresentation of the Role to be created
   * @return JsonArray with the role details
   */
  @POST
  @Path("/realms/{realm}/roles")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonArray> createRole(@HeaderParam("Authorization") String bearerToken,
      @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId, RoleRepresentation role);

  /**
   * Gets a role by name
   *
   * @param roleName id of the role to be queried
   * @return JsonArray with the role details
   */
  @GET
  @Path("/realms/{realm}/roles")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonArray> getRole(@HeaderParam("Authorization") String bearerToken,
      @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId, @QueryParam("search") String roleName);

  /**
   * Updates a role
   *
   * @param roleId id of the Role to be deleted
   * @return JsonArray with the role details
   */
  @PUT
  @Path("/realms/{realm}/roles-by-id/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonArray> updateRole(@HeaderParam("Authorization") String bearerToken,
      @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId, @PathParam("id") String roleId,
      RoleRepresentation role);

  /**
   * Deletes a role
   *
   * @param roleId id of the Role to be deleted
   * @return JsonArray with the role details
   */
  @DELETE
  @Path("/realms/{realm}/roles-by-id/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonArray> deleteRole(@HeaderParam("Authorization") String bearerToken,
      @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId, @PathParam("id") String roleId);

}
