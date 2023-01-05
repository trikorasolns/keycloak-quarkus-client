package com.trikorasolutions.keycloak.client;

import com.trikorasolutions.keycloak.client.bl.KeycloakClientLogic;
import com.trikorasolutions.keycloak.client.bl.KeycloakGroupLogic;
import com.trikorasolutions.keycloak.client.dto.RoleRepresentation;
import com.trikorasolutions.keycloak.client.dto.TrikoraGroupRepresentation;
import com.trikorasolutions.keycloak.client.dto.KeycloakUserRepresentation;
import com.trikorasolutions.keycloak.client.exception.NoSuchGroupException;
import io.quarkus.test.TestReactiveTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.UniAsserter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusTest
@TestReactiveTransaction
public final class LogicGroupTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogicGroupTest.class);

  @Inject
  private KeycloakClientLogic blClient;

  @Inject
  private KeycloakGroupLogic blGroup;

  @Inject
  private TrikoraKeycloakClientInfo tkrKcCli;

  @Test
  public void testCreateGroupOk(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(tkrKcCli.getAdm(), tkrKcCli.getAdm());
    final TrikoraGroupRepresentation newGroup = new TrikoraGroupRepresentation("TEST_CREATE");

    asserter
        .execute(
            () -> blGroup.deleteGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.getName()))
        .assertThat(
            () -> blGroup.createGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.getName(),null),
            group -> Assertions.assertThat(group.getName()).isEqualTo(newGroup.getName()))
    ;
  }

  @Test
  public void testCreateGroupAsTenantOk(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(tkrKcCli.getAdm(), tkrKcCli.getAdm());
    final TrikoraGroupRepresentation newGroup = new TrikoraGroupRepresentation("TENANT_TEST_ATTR");

    asserter
        .execute(
            () -> blGroup.deleteGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.getName()))
        .assertThat(
            () -> blGroup.createGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(),  newGroup.getName(),
                Map.of("tkr-tenant",List.of("TEST_INFO"))),
            group -> {
              /* Check if the attribute has been loaded into the session */
              Assertions.assertThat(group.getName()).isEqualTo(newGroup.getName());
            })
    ;
  }

  @Test
  public void testGroupInfoOk(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(tkrKcCli.getAdm(), tkrKcCli.getAdm());
    final TrikoraGroupRepresentation newGroup = new TrikoraGroupRepresentation("TENANT_TEST_INFO");
    final String userToEnroll = "mrsquare";

    asserter
        .execute(
            () -> blGroup.deleteGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.getName()))
        .execute(
            () -> blGroup.createGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.getName(), null))
        .execute( () -> blGroup.putUserInGroup(tkrKcCli.getRealmName(), accessToken,
            tkrKcCli.getClientId(), userToEnroll, newGroup.getName()))
        .assertThat(
            () -> blGroup.getGroupInfo(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.getName()),
            group -> {
              /* Check if the attribute has been loaded into the session */
              Assertions.assertThat(group.getName()).isEqualTo(newGroup.getName());
              Assertions.assertThat(group.roles).isNotNull();
              Assertions.assertThat(group.members).isNotNull()
                  .hasSizeGreaterThan(0);
            })
    ;
  }

  @Test
  public void testGroupInfoErr(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(tkrKcCli.getAdm(), tkrKcCli.getAdm());

    asserter.assertFailedWith(
        () -> blGroup.getGroupInfo(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
            "unknown"),
        NoSuchGroupException.class);
  }

  @Test
  public void testUpdateGroupOk(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(tkrKcCli.getAdm(), tkrKcCli.getAdm());
    final TrikoraGroupRepresentation newGroup = new TrikoraGroupRepresentation("TENANT_TEST_UPD");

    asserter
        .execute(
            () -> blGroup.deleteGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.getName()))
        .execute(
            () -> blGroup.createGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.getName(), null))
        .assertThat(
            () -> blGroup.updateGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.getName(),
                Map.of("tkr-tenant",List.of("TEST_UPD"))),
            group -> {
              /* Check if the attribute has been loaded into the session */
              Assertions.assertThat(group.getName()).isEqualTo(newGroup.getName());
            })
        .execute(
            () -> blGroup.addRolesToGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.getName(),
                "tenant_administrator","application_user"))
        .assertThat(
            () -> blGroup.getGroupRoles(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.getName()),
            roleRepresentationList -> {
              /* Check if the attribute has been loaded into the session */
              Assertions.assertThat(roleRepresentationList
                  .stream()
                  .map(RoleRepresentation::getName)
                  .collect(Collectors.toList())).contains("tenant_administrator","application_user");
            })
    ;
  }

  @Test
  public void testDeleteGroupOk(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(tkrKcCli.getAdm(), tkrKcCli.getAdm());
    final TrikoraGroupRepresentation newGroup = new TrikoraGroupRepresentation("TEST_DELETE");

    asserter
        .execute( // Delete the test user
            () -> blGroup.deleteGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.getName()))
        .execute( // Create a test user
            () -> blGroup.createGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.getName(),null))
        .assertThat(
            () -> blGroup.deleteGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.getName()),
            bool -> Assertions.assertThat(bool).isEqualTo(true))
    ;
  }

  @Test
  public void testDeleteGroupErr(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(tkrKcCli.getAdm(), tkrKcCli.getAdm());

    asserter
        .assertThat(
            () -> blGroup.deleteGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), "unknown"),
            bool -> Assertions.assertThat(bool).isEqualTo(false))
    ;
  }

  @Test
  public void testGroupListUsers(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(tkrKcCli.getAdm(), tkrKcCli.getAdm());
    final TrikoraGroupRepresentation newGroup = new TrikoraGroupRepresentation("TEST_LIST");
    final String userToEnroll = "mrsquare";

    asserter
        .execute(
            () -> blGroup.deleteGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.getName()))
        .assertThat(
            () -> blGroup.createGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.getName(),null),
            group -> Assertions.assertThat(group.getName()).isEqualTo(newGroup.getName()))
        .execute( // Put a new user in the group
            () -> blGroup.putUserInGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), userToEnroll, newGroup.getName()))
        .assertThat(
            () -> blGroup.getGroupMembers(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.getName()),
            listOfUser -> {
              List<String> usernameList = listOfUser.stream()
                  .map(user -> user.username)
                  .collect(Collectors.toList());
              Assertions.assertThat(usernameList).isNotEmpty();
              Assertions.assertThat(usernameList).contains(userToEnroll);
            })
    ;
  }

  @Test
  public void testPutAndRemoveUserInGroup(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(tkrKcCli.getAdm(), tkrKcCli.getAdm());
    final String userToEnroll = "mrsquare";
    final TrikoraGroupRepresentation newGroup = new TrikoraGroupRepresentation("TEST_PUT_REMOVE");

    asserter
        .execute(
            () -> blGroup.deleteGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.getName()))
        .assertThat(
            () -> blGroup.createGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.getName(),null),
            group -> Assertions.assertThat(group.getName()).isEqualTo(newGroup.getName()))
        .assertThat( // Put a new user in the group
            () -> blGroup.putUserInGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), userToEnroll, newGroup.getName()),
            user -> {
              Assertions.assertThat(user.username).isEqualTo(userToEnroll);
              Assertions.assertThat(user.groups.stream()
                  .map(TrikoraGroupRepresentation::getName)
                  .collect(Collectors.toList())).contains(newGroup.getName());
            })
        .assertThat( // Check if the change has been persisted in keycloak
            () -> blGroup.getGroupMembers(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.getName()),
            userList -> {
              List<String> usernameList = userList.stream()
                  .map(KeycloakUserRepresentation::getUsername)
                  .collect(Collectors.toList());
              Assertions.assertThat(usernameList).isNotEmpty();
              Assertions.assertThat(usernameList).contains(userToEnroll);
            })
        .assertThat( // Kick the user out of the group
            () -> blGroup.deleteUserFromGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), userToEnroll, newGroup.getName()),
            user -> Assertions.assertThat(user.username).isEqualTo(userToEnroll))
        .assertThat( // Check if the change has been persisted in keycloak
            () -> blGroup.getGroupMembers(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.getName()),
            userList -> {
              List<String> usernameList = userList.stream()
                  .map(KeycloakUserRepresentation::getUsername)
                  .collect(Collectors.toList());
              Assertions.assertThat(usernameList).isNotNull();
              Assertions.assertThat(usernameList).doesNotContain(userToEnroll);
            })
    ;
  }

}
