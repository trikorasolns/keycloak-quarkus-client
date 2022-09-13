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
    keycloakClientLogic.deleteRole(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
            newRole.name)
        .await().indefinitely();
    RoleRepresentation logicResponse = keycloakClientLogic.createRole(
            tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(), newRole)
        .await().indefinitely();

    assertThat(logicResponse.name, is(newRole.name));
    assertThat(logicResponse.description, is(newRole.description));
    assertThat(logicResponse.clientRole, is(false));


    //UPD
    newRole.description = "I have been updated";
     logicResponse = keycloakClientLogic.updateRole(
            tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(), newRole.name, newRole)
        .await().indefinitely();
     LOGGER.warn("UPD:{}", logicResponse);
     assertThat(logicResponse.description, is(newRole.description));
  }

  @Test
  public void testGetAllRoles() {
    String accessToken = tkrKcCli.getAccessToken(ADM, ADM);

    List<RoleRepresentation> logicResponse = keycloakClientLogic.listAllRoles(
            tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId())
        .await().indefinitely();
    assertThat(logicResponse.size(),is(greaterThanOrEqualTo(1)));
    //LOGGER.warn("TEST {}", logicResponse);
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