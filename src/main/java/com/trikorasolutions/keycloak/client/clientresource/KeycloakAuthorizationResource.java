package com.trikorasolutions.keycloak.client.clientresource;

import io.smallrye.mutiny.Uni;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/auth/realms")
@RegisterRestClient(configKey = "keycloak-api")
public interface KeycloakAuthorizationResource {

  /**
   * This method only returns personal information about the user, it does not provide. any
   * information regarding the status of the user in the keycloak application.
   *
   * @param bearerToken access token provided by the  keycloak's SecurityIdentity.
   * @param realm       the realm name in which the users are going to be queried.
   * @param grantType   kind of authentication method.
   * @param clientId    id of the client (service name).
   * @return a JsonObject inside a Uni wrapper with the user's information.
   */
  @GET
  @Path("/{realm}/protocol/openid-connect/userinfo")
  @Produces("application/json")
  Uni<JsonObject> userInfo(@HeaderParam("Authorization") String bearerToken,
      @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId);

  /**
   * Get the access token of the current user.
   *
   * @param realm     the realm name in which the users are going to be queried.
   * @param grantType kind of authentication method.
   * @param clientId  id of the client (service name).
   * @param secret    secret of the client (the one given by KC).
   * @return The token from Keycloak
   */
  @POST
  @Path("/{realm}/protocol/openid-connect/token")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<JsonObject> getToken(@PathParam("realm") String realm,
      @QueryParam("grant_type") String grantType,
      @QueryParam("client_id") String clientId, @QueryParam("client_secret") String secret);

}
