package com.trikorasolutions.keycloak.client;

import com.trikorasolutions.keycloak.client.bl.KeycloakClientLogic;
import com.trikorasolutions.keycloak.client.dto.GroupRepresentation;
import com.trikorasolutions.keycloak.client.dto.KeycloakUserRepresentation;
import com.trikorasolutions.keycloak.client.dto.RoleRepresentation;
import com.trikorasolutions.keycloak.client.exception.NoSuchGroupException;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static com.trikorasolutions.keycloak.client.TrikoraKeycloakClientInfo.ADM;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;

@QuarkusTest
public class LogicGroupTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogicGroupTest.class);

  @Inject
  KeycloakClientLogic keycloakClientLogic;

  @Inject
  TrikoraKeycloakClientInfo tkrKcCli;


  @Test
  public void testCreateGroupOk() {
    String accessToken = tkrKcCli.getAccessToken(ADM);
    GroupRepresentation newGroup = new GroupRepresentation("TEST_CREATE");
    // LOGGER.info("test{}", newGroup);
    GroupRepresentation logicResponse;
    boolean logicResponse2;

    keycloakClientLogic.deleteGroup(tkrKcCli.getRealmName(), accessToken,
        tkrKcCli.getClientId(), newGroup.name).await().indefinitely();

    logicResponse = keycloakClientLogic.createGroup(tkrKcCli.getRealmName(), accessToken,
        tkrKcCli.getClientId(), newGroup).await().indefinitely();
    assertThat(logicResponse.getName(), is(newGroup.name));

    logicResponse2 = keycloakClientLogic.deleteGroup(tkrKcCli.getRealmName(), accessToken,
        tkrKcCli.getClientId(), newGroup.name).await().indefinitely();
    assertThat(logicResponse2, is(true));

  }

  @Test
  public void testGroupInfoOk() {
    String accessToken = tkrKcCli.getAccessToken(ADM);
    GroupRepresentation logicResponse;

    logicResponse = keycloakClientLogic.getGroupInfo(tkrKcCli.getRealmName(), accessToken,
        tkrKcCli.getClientId(),
        "TENANT_TEST").await().indefinitely();

    assertThat(logicResponse.getName(), is("TENANT_TEST"));
  }

  @Test
  public void testGroupInfoErr() {
    String accessToken = tkrKcCli.getAccessToken(ADM);

    try {
      keycloakClientLogic.getGroupInfo(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
          "unknown").onFailure(NoSuchGroupException.class).transform(x -> {
        throw (NoSuchGroupException) x;
      }).await().indefinitely();
      fail();
    } catch (NoSuchGroupException ex) {
      assertThat(ex.getClass(), is(NoSuchGroupException.class));
      assertThat(ex.getMessage(), containsString("unknown"));
    }
  }

  @Test
  public void testGroupListUsers() {
    String accessToken = tkrKcCli.getAccessToken(ADM);
    List<KeycloakUserRepresentation> logicResponse;

    logicResponse = keycloakClientLogic.getGroupMembers(tkrKcCli.getRealmName(), accessToken,
        tkrKcCli.getClientId(), "TENANT_TEST").await().indefinitely();

    List<String> userRepresentation = logicResponse.stream()
        .map(user -> user.username)
        .collect(Collectors.toList());
    assertThat(userRepresentation.size(), greaterThanOrEqualTo(1));
    assertThat(userRepresentation, hasItem(ADM));
  }

  @Test
  public void testPutAndRemoveUserInGroup() {
    String accessToken = tkrKcCli.getAccessToken(ADM);
    KeycloakUserRepresentation logicResponse;
    List<KeycloakUserRepresentation> logicResponse2;

    // Put a new user in the group
    logicResponse = keycloakClientLogic.putUserInGroup(tkrKcCli.getRealmName(), accessToken,
        tkrKcCli.getClientId(),
        "mrsquare", "TENANT_TEST").await().indefinitely();

    assertThat(logicResponse.username, is("mrsquare"));
    assertThat(logicResponse.groups.stream()
        .map(GroupRepresentation::getName)
        .collect(Collectors.toList()), hasItem("TENANT_TEST"));

    // Check if the change has been persisted in keycloak
    logicResponse2 = keycloakClientLogic.getGroupMembers(tkrKcCli.getRealmName(), accessToken,
        tkrKcCli.getClientId(), "TENANT_TEST").await().indefinitely();
    List<String> userRepresentation = logicResponse2.stream()
        .map(user -> user.username)
        .collect(Collectors.toList());
    assertThat(userRepresentation.size(), greaterThanOrEqualTo(1));
    assertThat(userRepresentation, hasItem("mrsquare"));

    // Kick the user out of the group
    logicResponse = keycloakClientLogic.deleteUserFromGroup(tkrKcCli.getRealmName(), accessToken,
        tkrKcCli.getClientId(),
        "mrsquare", "TENANT_TEST").await().indefinitely();
    assertThat(logicResponse.username, is("mrsquare"));

    // Check if the change has been persisted in keycloak
    logicResponse2 = keycloakClientLogic.getGroupMembers(tkrKcCli.getRealmName(), accessToken,
        tkrKcCli.getClientId(), "TENANT_TEST").await().indefinitely();
    userRepresentation = logicResponse2.stream()
        .map(user -> user.username)
        .collect(Collectors.toList());
    assertThat(userRepresentation.size(), greaterThanOrEqualTo(0));
    assertThat(userRepresentation, not(hasItem("mrsquare")));
  }

  @Test
  public void testPutRemoveRoleFromGroup() {
    String accessToken = tkrKcCli.getAccessToken(ADM);
    GroupRepresentation newGroup = new GroupRepresentation("TEST_ROLE");
    String[] roles = {"application_user", "hr", "space_manager"};

    GroupRepresentation logicResponse;
    List<RoleRepresentation> logicResponse2;
    // Generate the group
    keycloakClientLogic.deleteGroup(tkrKcCli.getRealmName(), accessToken,
        tkrKcCli.getClientId(), newGroup.name).await().indefinitely();
    logicResponse = keycloakClientLogic.createGroup(tkrKcCli.getRealmName(), accessToken,
        tkrKcCli.getClientId(), newGroup).await().indefinitely();
    assertThat(logicResponse.getName(), is(newGroup.name));

    // Add the roles to the group
    keycloakClientLogic.addRolesToGroup(tkrKcCli.getRealmName(), accessToken,
        tkrKcCli.getClientId(), newGroup.name, roles).await().indefinitely();

    logicResponse2 = keycloakClientLogic.getGroupRoles(tkrKcCli.getRealmName(), accessToken,
        tkrKcCli.getClientId(), newGroup.name).await().indefinitely();

    // Check that all the roles has been added correctly
    assertThat(logicResponse2
        .stream()
        .map(RoleRepresentation::getName)
        .collect(Collectors.toList()), containsInAnyOrder(roles));

    // Remove the roles from the group
    keycloakClientLogic.removeRolesFromGroup(tkrKcCli.getRealmName(), accessToken,
        tkrKcCli.getClientId(), newGroup.name, roles).await().indefinitely();

    logicResponse2 = keycloakClientLogic.getGroupRoles(tkrKcCli.getRealmName(), accessToken,
        tkrKcCli.getClientId(), newGroup.name).await().indefinitely();

    // Check that all the roles has been removed correctly
    assertThat(logicResponse2
        .stream()
        .map(RoleRepresentation::getName)
        .collect(Collectors.toList()), not(containsInAnyOrder(roles)));

    //TearDown
    keycloakClientLogic.deleteGroup(tkrKcCli.getRealmName(), accessToken,
        tkrKcCli.getClientId(), newGroup.name).await().indefinitely();

  }

}