package com.trikorasolutions.keycloak.client;

import com.trikorasolutions.keycloak.client.bl.KeycloakClientLogic;
import com.trikorasolutions.keycloak.client.dto.RoleRepresentation;
import com.trikorasolutions.keycloak.client.dto.UserRepresentation;
import io.quarkus.test.TestReactiveTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.UniAsserter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;

import static com.trikorasolutions.keycloak.client.TrikoraKeycloakClientInfo.ADM;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestReactiveTransaction
public class LogicRoleTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogicRoleTest.class);

  @Inject
  KeycloakClientLogic clientLogic;

  @Inject
  TrikoraKeycloakClientInfo tkrKcCli;

  @Test
  public void testCreateRole(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);
    RoleRepresentation newRole = new RoleRepresentation("test-create-role",
        "test-create-role-desc");

    asserter.execute(
            () -> clientLogic.deleteRole(tkrKcCli.getRealmName(), tkrKcCli.getAccessToken(ADM, ADM),
                tkrKcCli.getClientId(), newRole.name))
        .assertThat(
            () -> clientLogic.createRole(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newRole),
            role -> {
              assertThat(role.name).isEqualTo(newRole.name);
              assertThat(role.description).isEqualTo(newRole.description);
              assertThat(role.clientRole).isEqualTo(Boolean.FALSE);
              assertThat(role.composite).isEqualTo(Boolean.FALSE);
              assertThat(role.containerId).isEqualTo(tkrKcCli.getRealmName());
            }
        )
    ;
  }

  @Test
  public void testUpdateRole(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);
    RoleRepresentation newRole = new RoleRepresentation("test-update-role",
        "test-update-role-desc");

    asserter.execute(() -> this.clientLogic.deleteRole(tkrKcCli.getRealmName(), accessToken,
            tkrKcCli.getClientId(), newRole.name))
        .assertThat(
            () -> clientLogic.createRole(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newRole),
            role -> {
              assertThat(role.name).isEqualTo(newRole.name);
              assertThat(role.description).isEqualTo(newRole.description);
              assertThat(role.clientRole).isEqualTo(Boolean.FALSE);
              assertThat(role.composite).isEqualTo(Boolean.FALSE);
              assertThat(role.containerId).isEqualTo(tkrKcCli.getRealmName());
            }
        ).assertThat(
            () -> clientLogic.updateRole(
                tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(), newRole.name, newRole),
            role -> {
              assertThat(role.name).isEqualTo(newRole.name);
              assertThat(role.description).isEqualTo(newRole.description);
            }
        )
    ;
  }

  @Test
  public void testGetAllRoles(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);

    asserter.assertThat(() -> clientLogic.listAllRoles(tkrKcCli.getRealmName(), accessToken,
            tkrKcCli.getClientId()),
        listOfRoles -> assertThat(listOfRoles).isNotEmpty());
  }

  @Test
  public void testGetRoleUsers(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);

    asserter.assertThat(
        () -> clientLogic.getAllUsersInAssignedRole(tkrKcCli.getRealmName(), accessToken,
            tkrKcCli.getClientId(), "hr"),
        listOfUser -> assertThat(listOfUser).isNotEmpty());

  }

  @Test
  public void testGetUserRoles(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);

    asserter.assertThat(
        () -> clientLogic.getUserRoles(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
            ADM),
        listOfRoles -> assertThat(listOfRoles).isNotEmpty());
  }

  @Test
  public void testGetAllUsersInEffectiveRole(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);

    asserter.assertThat(
        () -> clientLogic.getAllUserInEffectiveRole(tkrKcCli.getRealmName(), accessToken,
            tkrKcCli.getClientId(), "project_manager"),
        listOfUsers -> assertThat(listOfUsers).isNotEmpty());
  }

  @Test
  public void testGetToken(UniAsserter asserter) {

    asserter.assertThat(
        () -> clientLogic.getTokenForUser(tkrKcCli.getRealmName(), tkrKcCli.getClientId(),
            tkrKcCli.getClientSecret()),
        tok -> assertThat(tok).isNotEmpty());
  }

  @Test
  public void testDeleteRoleOk(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);
    RoleRepresentation newRole = new RoleRepresentation("test-del-role",
        "test-del-role-desc");

    asserter
        .execute( // Delete the test user
            () -> clientLogic.deleteRole(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newRole.name))
        .execute( // Create a test user
            () -> clientLogic.createRole(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newRole))
        .assertThat(
            () -> clientLogic.deleteRole(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newRole.name),
            bool -> Assertions.assertThat(bool).isEqualTo(true))
    ;
  }

  @Test
  public void testDeleteRoleErr(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);

    asserter
        .assertThat(
            () -> clientLogic.deleteRole(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), "unknown"),
            bool -> Assertions.assertThat(bool).isEqualTo(false))
    ;
  }
}
