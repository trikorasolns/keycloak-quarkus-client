package com.trikorasolutions.keycloak.client;

import com.trikorasolutions.keycloak.client.bl.KeycloakClientLogic;
import com.trikorasolutions.keycloak.client.dto.GroupRepresentation;
import com.trikorasolutions.keycloak.client.dto.KeycloakUserRepresentation;
import com.trikorasolutions.keycloak.client.dto.RoleRepresentation;
import com.trikorasolutions.keycloak.client.exception.NoSuchGroupException;
import io.quarkus.test.TestReactiveTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.UniAsserter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static com.trikorasolutions.keycloak.client.TrikoraKeycloakClientInfo.ADM;

@QuarkusTest
@TestReactiveTransaction
public class LogicGroupTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogicGroupTest.class);

  @Inject
  KeycloakClientLogic clientLogic;

  @Inject
  TrikoraKeycloakClientInfo tkrKcCli;


  @Test
  public void testCreateGroupOk(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);
    final GroupRepresentation newGroup = new GroupRepresentation("TEST_CREATE");

    asserter
        .execute(
            () -> clientLogic.deleteGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.name))
        .assertThat(
            () -> clientLogic.createGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup),
            group -> Assertions.assertThat(group.name).isEqualTo(newGroup.name))
    ;
  }

  @Test
  public void testGroupInfoErr(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);

    asserter.assertFailedWith(
        () -> clientLogic.getGroupInfo(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
            "unknown"),
        NoSuchGroupException.class);
  }

  @Test
  public void testDeleteGroupOk(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);
    final GroupRepresentation newGroup = new GroupRepresentation("TEST_DELETE");

    asserter
        .execute( // Delete the test user
            () -> clientLogic.deleteGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.name))
        .execute( // Create a test user
            () -> clientLogic.createGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup))
        .assertThat(
            () -> clientLogic.deleteGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.name),
            bool -> Assertions.assertThat(bool).isEqualTo(true))
    ;
  }

  @Test
  public void testDeleteGroupErr(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);

    asserter
        .assertThat(
            () -> clientLogic.deleteGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), "unknown"),
            bool -> Assertions.assertThat(bool).isEqualTo(false))
    ;
  }

  @Test
  public void testGroupListUsers(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);
    final GroupRepresentation newGroup = new GroupRepresentation("TEST_LIST");
    final String userToEnroll = "mrsquare";

    asserter
        .execute(
            () -> clientLogic.deleteGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.name))
        .assertThat(
            () -> clientLogic.createGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup),
            group -> Assertions.assertThat(group.name).isEqualTo(newGroup.name))
        .execute( // Put a new user in the group
            () -> clientLogic.putUserInGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), userToEnroll, newGroup.name))
        .assertThat(
            () -> clientLogic.getGroupMembers(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.name),
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
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);
    final String userToEnroll = "mrsquare";
    final GroupRepresentation newGroup = new GroupRepresentation("TEST_PUT_REMOVE");

    asserter
        .execute(
            () -> clientLogic.deleteGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.name))
        .assertThat(
            () -> clientLogic.createGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup),
            group -> Assertions.assertThat(group.name).isEqualTo(newGroup.name))
        .assertThat( // Put a new user in the group
            () -> clientLogic.putUserInGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), userToEnroll, newGroup.name),
            user -> {
              Assertions.assertThat(user.username).isEqualTo(userToEnroll);
              Assertions.assertThat(user.groups.stream()
                  .map(GroupRepresentation::getName)
                  .collect(Collectors.toList())).contains(newGroup.name);
            })
        .assertThat( // Check if the change has been persisted in keycloak
            () -> clientLogic.getGroupMembers(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.name),
            userList -> {
              List<String> usernameList = userList.stream()
                  .map(KeycloakUserRepresentation::getUsername)
                  .collect(Collectors.toList());
              Assertions.assertThat(usernameList).isNotEmpty();
              Assertions.assertThat(usernameList).contains(userToEnroll);
            })
        .assertThat( // Kick the user out of the group
            () -> clientLogic.deleteUserFromGroup(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), userToEnroll, newGroup.name),
            user -> Assertions.assertThat(user.username).isEqualTo(userToEnroll))
        .assertThat( // Check if the change has been persisted in keycloak
            () -> clientLogic.getGroupMembers(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newGroup.name),
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
