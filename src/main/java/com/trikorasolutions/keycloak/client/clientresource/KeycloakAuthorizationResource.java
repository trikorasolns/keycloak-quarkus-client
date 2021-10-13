package com.trikorasolutions.keycloak.client.clientresource;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.json.JsonObject;
import javax.ws.rs.*;

@Path("/auth/realms")
@RegisterRestClient(configKey = "keycloak-api")
public interface KeycloakAuthorizationResource {

  /**
   * This method only returns personal information about the user, it does not provide.
   * any information regarding the status of the user in the keycloak application.
   * @param bearerToken access token provided by the  keycloak's SecurityIdentity.
   * @param realm the realm name in which the users are going to be queried.
   * @param grantType kind of authentication method.
   * @param clientId id of the client (service name).
   *
   * @return a JsonObject inside a Uni wrapper with the user's information.
   */
  @GET
  @Path("/{realm}/protocol/openid-connect/userinfo")
  @Produces("application/json")
  Uni<JsonObject> userInfo(@HeaderParam("Authorization") String bearerToken,
                          @PathParam("realm") String realm, @QueryParam("grant_type") String grantType,
                          @QueryParam("client_id") String clientId);

}
