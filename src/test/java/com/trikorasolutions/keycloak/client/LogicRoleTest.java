package com.trikorasolutions.keycloak.client;

import com.trikorasolutions.keycloak.client.bl.KeycloakClientLogic;
import com.trikorasolutions.keycloak.client.dto.GroupRepresentation;
import com.trikorasolutions.keycloak.client.dto.KeycloakUserRepresentation;
import com.trikorasolutions.keycloak.client.dto.RoleRepresentation;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Arrays;
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
  public void testGetRoleUsers() {
    String accessToken = tkrKcCli.getAccessToken(ADM);

    List<KeycloakUserRepresentation> logicResponse = keycloakClientLogic.getAllUsersInAssignedRole(
        tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(), "hr").await().indefinitely();
    assertThat(logicResponse.size(), is(greaterThanOrEqualTo(1)));
  }

  @Test
  public void testGetUserRoles() {
    String accessToken = tkrKcCli.getAccessToken(ADM);
    List<RoleRepresentation> logicResponse = keycloakClientLogic.getUserRoles(
        tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(), ADM).await().indefinitely();
    assertThat(logicResponse.size(), is(greaterThanOrEqualTo(1)));
  }

  @Test
  public void testGetAllUsersInEffectiveRole() {
    String accessToken = tkrKcCli.getAccessToken(ADM);

    Set<KeycloakUserRepresentation> logicResponse = keycloakClientLogic.getAllUserInEffectiveRole(
            tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(), "project_manager").await()
        .indefinitely();
    assertThat(logicResponse.size(), is(greaterThanOrEqualTo(1)));
  }

  @Test
  public void testPrintUserAndGroup() {
    String accessToken = tkrKcCli.getAccessToken(ADM);

    KeycloakUserRepresentation logicResponse = keycloakClientLogic.getUserInfo(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(), ADM).await().indefinitely();
    LOGGER.info("USER: \n{} ",logicResponse);
    GroupRepresentation logicResponse2 = keycloakClientLogic.getGroupInfo(
            tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(), "Project Manager").await()
        .indefinitely();
    LOGGER.info("GROUP: \n{} ",logicResponse2);

    List<GroupRepresentation> logicResponse3 = keycloakClientLogic.listAllGroups(
            tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId()).await()
        .indefinitely();
    LOGGER.info("All GROUP: \n{} ",logicResponse3);
  }


}