package com.trikorasolutions.keycloak.client;

import com.trikorasolutions.keycloak.client.bl.KeycloakClientLogic;
import com.trikorasolutions.keycloak.client.dto.KeycloakUserRepresentation;
import com.trikorasolutions.keycloak.client.dto.RoleRepresentation;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

import static com.trikorasolutions.keycloak.client.TrikoraKeycloakClientInfo.ADM;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

@QuarkusTest
public class LogicRoleTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogicRoleTest.class);

  @Inject
  KeycloakClientLogic keycloakClientLogic;

  @Inject
  TrikoraKeycloakClientInfo tkrKcCli;

  @Test
  public void testCreateRole() {
    String accessToken = tkrKcCli.getAccessToken(ADM, ADM);

    RoleRepresentation newRole = new RoleRepresentation("test-create-role",
        "test-create-role-desc");
    keycloakClientLogic.deleteRole(
            tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(), newRole.name)
        .await()
        .indefinitely();
    RoleRepresentation logicResponse = keycloakClientLogic.createRole(
            tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(), newRole).await()
        .indefinitely();
    LOGGER.warn("TEST {}",logicResponse);
    assertThat(logicResponse.name, is(newRole.name));
    assertThat(logicResponse.description, is(newRole.description));
    assertThat(logicResponse.clientRole, is(false));
  }

  @Test
  public void testGetRoleUsers() {
    String accessToken = tkrKcCli.getAccessToken(ADM, ADM);

    List<KeycloakUserRepresentation> logicResponse = keycloakClientLogic.getAllUsersInAssignedRole(
        tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(), "hr").await().indefinitely();
    assertThat(logicResponse.size(), is(greaterThanOrEqualTo(1)));
  }

  @Test
  public void testGetUserRoles() {
    String accessToken = tkrKcCli.getAccessToken(ADM, ADM);
    List<RoleRepresentation> logicResponse = keycloakClientLogic.getUserRoles(
        tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(), ADM).await().indefinitely();
    assertThat(logicResponse.size(), is(greaterThanOrEqualTo(1)));
  }

  @Test
  public void testGetAllUsersInEffectiveRole() {
    String accessToken = tkrKcCli.getAccessToken(ADM, ADM);

    Set<KeycloakUserRepresentation> logicResponse = keycloakClientLogic.getAllUserInEffectiveRole(
            tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(), "project_manager").await()
        .indefinitely();
    assertThat(logicResponse.size(), is(greaterThanOrEqualTo(1)));
  }

  @Test
  public void testGetToken() {
    String tok = keycloakClientLogic.getTokenForUser(
            tkrKcCli.getRealmName(), tkrKcCli.getClientId(), tkrKcCli.getClientSecret()).await()
        .indefinitely();
    assertThat(tok.length(), is(greaterThanOrEqualTo(1)));
  }

}