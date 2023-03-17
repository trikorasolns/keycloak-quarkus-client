package com.trikorasolutions.keycloak.client.ut;

import static javax.ws.rs.core.Response.Status.CONFLICT;

import com.trikorasolutions.keycloak.client.MockKeycloakProfile;
import com.trikorasolutions.keycloak.client.bl.KeycloakClientLogic;
import com.trikorasolutions.keycloak.client.clientresource.KeycloakAuthAdminResource;
import com.trikorasolutions.keycloak.client.dto.RoleRepresentation;
import com.trikorasolutions.keycloak.client.dto.UserRepresentation;
import com.trikorasolutions.keycloak.client.dto.UserRepresentation.UserDtoCredential;
import com.trikorasolutions.keycloak.client.exception.DuplicatedUserException;
import com.trikorasolutions.keycloak.client.exception.NoSuchUserException;
import com.trikorasolutions.keycloak.client.it.TrikoraKeycloakClientInfo;
import io.quarkus.test.TestReactiveTransaction;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.vertx.UniAsserter;
import io.smallrye.mutiny.Uni;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.spi.JsonProvider;
import org.assertj.core.api.Assertions;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusTest
@TestReactiveTransaction
@TestInstance(Lifecycle.PER_CLASS)
@TestProfile(MockKeycloakProfile.class)
public class KeycloakClientTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakClientTest.class);

  @Inject
  KeycloakClientLogic clientLogic;

  @Inject
  TrikoraKeycloakClientInfo tkrKcCli;

  Map<String, UserRepresentation> userMap = new HashMap<>();

  @BeforeAll
  public void setUp() {
    UserRepresentation userRepresentation = new UserRepresentation();
    userRepresentation.setUsername("existinguser");
    userRepresentation.setEmail("exisitnguser@acme.org");
    userMap.put(userRepresentation.getUsername(), userRepresentation);

    KeycloakAuthAdminResource customMock = new KeycloakAuthAdminResource() {
      @Override
      public Uni<JsonArray> createUser(String bearerToken, String realm, String grantType,
          String clientId,
          UserRepresentation body) {
        if (userMap.containsKey(body.username)) {
          return Uni.createFrom()
              .failure(new ClientWebApplicationException(CONFLICT.getStatusCode()));
        } else {
          userMap.put(body.username, body);
          return Uni.createFrom().item(JsonProvider.provider().createArrayBuilder().add(
              JsonProvider.provider().createObjectBuilder()
                  .add("username", userRepresentation.getUsername())
                  .add("email", userRepresentation.getEmail()).build()).build());
        }
      }

      @Override
      public Uni<JsonArray> updateUser(String bearerToken, String realm, String grantType,
          String clientId, String id,
          UserRepresentation body) {
        return Uni.createFrom().item(JsonProvider.provider().createArrayBuilder().build());
      }

      @Override
      public Uni<JsonArray> resetPassword(String bearerToken, String realm, String grantType,
          String clientId, String id,
          UserDtoCredential body) {
        return Uni.createFrom().item(JsonProvider.provider().createArrayBuilder().build());
      }

      @Override
      public Uni<JsonArray> getUserInfo(String bearerToken, String realm, String grantType,
          String clientId, String username,
          Boolean exact) {
        if (userMap.containsKey(username)) {
          UserRepresentation userRepresentation = userMap.get(username);
          return Uni.createFrom().item(JsonProvider.provider().createArrayBuilder().add(
                  JsonProvider.provider().createObjectBuilder()
                      .add("username", userRepresentation.getUsername())
                      .add("email", userRepresentation.getEmail()).build())
              .build());
        } else {
          throw new NoSuchUserException(username);
        }
      }

      @Override
      public Uni<JsonArray> deleteUser(String bearerToken, String realm, String grantType,
          String clientId, String id) {
        return Uni.createFrom().item(JsonArray.EMPTY_JSON_ARRAY);
      }

      @Override
      public Uni<JsonArray> listAllUsers(String bearerToken, String realm, String grantType,
          String clientId, Integer first,
          Integer max) {
        return Uni.createFrom().item(JsonProvider.provider().createArrayBuilder().build());
      }

      @Override
      public Uni<JsonArray> listAllGroups(String bearerToken, String realm, String grantType,
          String clientId) {
        return Uni.createFrom().item(JsonProvider.provider().createArrayBuilder().build());
      }

      @Override
      public Uni<JsonArray> createGroup(String bearerToken, String realm, String grantType,
          String clientId, String group) {
        return Uni.createFrom().item(JsonProvider.provider().createArrayBuilder().build());
      }

      @Override
      public Uni<JsonArray> getGroupInfo(String bearerToken, String realm, String grantType,
          String clientId,
          String groupName, Boolean exact) {
        return Uni.createFrom().item(JsonProvider.provider().createArrayBuilder().build());
      }

      @Override
      public Uni<JsonArray> deleteGroup(String bearerToken, String realm, String grantType,
          String clientId, String id) {
        return Uni.createFrom().item(JsonProvider.provider().createArrayBuilder().build());
      }

      @Override
      public Uni<JsonArray> getGroupUsers(String bearerToken, String realm, String grantType,
          String clientId, String id,
          Integer first, Integer max) {
        return Uni.createFrom().item(JsonProvider.provider().createArrayBuilder().build());
      }

      @Override
      public Uni<JsonArray> getUserGroups(String bearerToken, String realm, String grantType,
          String clientId, String id) {
        return Uni.createFrom().item(JsonProvider.provider().createArrayBuilder().build());
      }

      @Override
      public Uni<JsonArray> putUserInGroup(String bearerToken, String realm, String grantType,
          String clientId, String id,
          String groupId) {
        return Uni.createFrom().item(JsonProvider.provider().createArrayBuilder().build());
      }

      @Override
      public Uni<JsonArray> deleteUserFromGroup(String bearerToken, String realm, String grantType,
          String clientId,
          String id, String groupId) {
        return Uni.createFrom().item(JsonProvider.provider().createArrayBuilder().build());
      }

      @Override
      public Uni<JsonArray> getAllUsersInRole(String bearerToken, String realm, String grantType,
          String clientId,
          String roleName) {
        return Uni.createFrom().item(JsonProvider.provider().createArrayBuilder().build());
      }

      @Override
      public Uni<JsonArray> getAllGroupsInRole(String bearerToken, String realm, String grantType,
          String clientId,
          String roleName) {
        return Uni.createFrom().item(JsonProvider.provider().createArrayBuilder().build());
      }

      @Override
      public Uni<JsonArray> getUserRoles(String bearerToken, String realm, String grantType,
          String clientId, String userId) {
        return Uni.createFrom().item(JsonProvider.provider().createArrayBuilder().build());
      }

      @Override
      public Uni<JsonArray> addRolesToGroup(String bearerToken, String realm, String grantType,
          String clientId,
          String groupId, String roles) {
        return Uni.createFrom().item(JsonProvider.provider().createArrayBuilder().build());
      }

      @Override
      public Uni<JsonArray> removeRolesToGroup(String bearerToken, String realm, String grantType,
          String clientId,
          String groupId, String roles) {
        return Uni.createFrom().item(JsonProvider.provider().createArrayBuilder().build());
      }

      @Override
      public Uni<JsonArray> removeRolesFromGroup(String bearerToken, String realm, String grantType,
          String clientId,
          String groupId, RoleRepresentation[] roles) {
        return Uni.createFrom().item(JsonProvider.provider().createArrayBuilder().build());
      }

      @Override
      public Uni<JsonArray> getGroupRoles(String bearerToken, String realm, String grantType,
          String clientId,
          String groupId) {
        return Uni.createFrom().item(JsonProvider.provider().createArrayBuilder().build());
      }

      @Override
      public Uni<JsonArray> listRoles(String bearerToken, String realm, String grantType,
          String clientId) {
        return Uni.createFrom().item(JsonProvider.provider().createArrayBuilder().build());
      }

      @Override
      public Uni<JsonArray> createRole(String bearerToken, String realm, String grantType,
          String clientId,
          RoleRepresentation rep) {
        return Uni.createFrom().item(JsonProvider.provider().createArrayBuilder().build());
      }

      @Override
      public Uni<JsonArray> getAllRoles(String bearerToken, String realm, String grantType,
          String clientId) {
        return Uni.createFrom().item(JsonProvider.provider().createArrayBuilder().build());
      }

      @Override
      public Uni<JsonArray> getRoleInfo(String bearerToken, String realm, String grantType,
          String clientId, String roleName,
          Boolean exact) {
        return Uni.createFrom().item(JsonProvider.provider().createArrayBuilder().build());
      }

      @Override
      public Uni<JsonArray> updateRole(String bearerToken, String realm, String grantType,
          String clientId, String roleName,
          RoleRepresentation rep) {
        return Uni.createFrom().item(JsonProvider.provider().createArrayBuilder().build());
      }

      @Override
      public Uni<JsonArray> deleteRole(String bearerToken, String realm, String grantType,
          String clientId, String roleName) {
        return Uni.createFrom().item(JsonProvider.provider().createArrayBuilder().build());
      }
    };
    QuarkusMock.installMockForType(customMock, KeycloakAuthAdminResource.class, RestClient.LITERAL);
  }

  @Test
  public void testCreateUserOk(UniAsserter asserter) {
    UserRepresentation userRepresentation = new UserRepresentation();
    userRepresentation.setUsername("testCreateOk");
    userRepresentation.setEmail("test.create.ok@acme.org");
    asserter
        .assertThat( // Create a test user
            () -> clientLogic.createUser("trikorasolutions", "xxxx",
                tkrKcCli.getClientId(), userRepresentation),
            user -> {
              Assertions.assertThat(user.username).isEqualTo(userRepresentation.getUsername());
              Assertions.assertThat(user.email).isEqualTo(userRepresentation.getEmail());
            })
    ;
  }

  @Test
  public void testCreateUserDuplicate(UniAsserter asserter) {
    UserRepresentation userRepresentation = new UserRepresentation();
    userRepresentation.setUsername("existinguser");
    userRepresentation.setEmail("exisitnguser@acme.org");
    asserter
        .assertFailedWith( // Create a test user
            () -> clientLogic.createUser("trikorasolutions", "xxxx",
                tkrKcCli.getClientId(), userRepresentation),
            DuplicatedUserException.class)
    ;
  }

}