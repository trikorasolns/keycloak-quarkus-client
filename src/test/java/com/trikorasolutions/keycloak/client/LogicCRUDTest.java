package com.trikorasolutions.keycloak.client;

import com.trikorasolutions.keycloak.client.bl.KeycloakClientLogic;
import com.trikorasolutions.keycloak.client.dto.UserRepresentation;
import com.trikorasolutions.keycloak.client.exception.*;
import io.quarkus.test.TestReactiveTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.UniAsserter;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.trikorasolutions.keycloak.client.TrikoraKeycloakClientInfo.ADM;

@QuarkusTest
@TestReactiveTransaction
public class LogicCRUDTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogicCRUDTest.class);
  private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
      Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
  private static final Pattern VALID_KEYCLOAK_ID_REGEX =
      Pattern.compile("^[A-Z0-9]+-[A-Z0-9]+-[A-Z0-9]+-[A-Z0-9]+-[A-Z0-9]+$",
          Pattern.CASE_INSENSITIVE);

  private static final Condition<String> keycloakId = new Condition<>(
      s -> s != null && s.length() == 36 && VALID_KEYCLOAK_ID_REGEX.matcher(s).find(),
      "The provided string does not match the pattern of an standard keycloak Id");
  private static final Condition<String> standardEmail = new Condition<>(
      s -> s != null && VALID_EMAIL_ADDRESS_REGEX.matcher(s).find(),
      "The provided string does not match the pattern of an standard email\"");

  @Inject
  KeycloakClientLogic clientLogic;

  @Inject
  TrikoraKeycloakClientInfo tkrKcCli;


  @Test
  public void testCreateUserOk(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);
    final UserRepresentation newUser = new UserRepresentation("test", "create",
        "testcreate@trikorasolutions.com", true,
        "testcreate", "testcreate");

    asserter
        .execute( // Delete the test user
            () -> clientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser.username))
        .assertThat( // Create a test user
            () -> clientLogic.createUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser),
            user -> {
              Assertions.assertThat(user.username).isEqualTo(newUser.username);
              Assertions.assertThat(user.email).isEqualTo(newUser.email);
            })
    ;
  }

  @Test
  public void testCreateUserDuplicatedErr(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);
    UserRepresentation newUser = new UserRepresentation("mr", "rectangle",
        "mrrectangule@trikorasolutions.com", true,
        "mrrectangule", "mrrectangule");

    asserter
        .execute( // Delete the test user
            () -> clientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser.username))
        .assertThat( // Create a test user
            () -> clientLogic.createUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser),
            user -> {
              Assertions.assertThat(user.username).isEqualTo(newUser.username);
              Assertions.assertThat(user.email).isEqualTo(newUser.email);
            })
        .assertFailedWith(
            () -> clientLogic.createUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser),
            DuplicatedUserException.class
        )
    ;
  }

  @Test
  public void testCreateUserInvalidTokenErr(UniAsserter asserter) {
    String accessToken = tkrKcCli.getAccessToken(ADM, ADM);
    UserRepresentation newUser = new UserRepresentation("mr", "rectangle",
        "mrrectangule@trikorasolutions.com", true,
        "mrrectangule", "mrrectangule");

    asserter
        .execute( // Delete the test user
            () -> clientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser.username))
        .assertFailedWith(
            () -> clientLogic.createUser(tkrKcCli.getRealmName(), "BAD TOKEN",
                tkrKcCli.getClientId(), newUser),
            InvalidTokenException.class)
    ;
  }

  @Test
  public void testCreateUserNotFoundErr(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);
    UserRepresentation newUser = new UserRepresentation("mr", "rectangle",
        "mrrectangule@trikorasolutions.com", true,
        "mrrectangule", "mrrectangule");

    asserter
        .assertFailedWith(
            () -> clientLogic.createUser("realm_is_not_defined", accessToken,
                "client_is_not_defined", newUser),
            ClientNotFoundException.class)
    ;
  }

  @Test
  public void testCreateUserInvalidUserErr(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);
    UserRepresentation newUser = new UserRepresentation(null, null, null, true, null);

    asserter
        .execute( // Delete the test user
            () -> clientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser.username))
        .assertFailedWith(
            () -> clientLogic.createUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser),
            ArgumentsFormatException.class)
    ;
  }

  @Test
  public void testCreateWithOutEmailOk(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);
    UserRepresentation newUser = new UserRepresentation("mr", "rectangle", null, true,
        "mrrectangule", "mrrectangule");

    asserter
        .execute( // Delete the test user
            () -> clientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser.username))
        .execute( // Create a test user
            () -> clientLogic.createUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser))
        .assertThat(
            () -> clientLogic.getUserInfo(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser.username),
            user -> {
              Assertions.assertThat(user.firstName).isEqualTo(newUser.firstName);
              Assertions.assertThat(user.lastName).isEqualTo(newUser.lastName);
              Assertions.assertThat(user.email).isNull();
              Assertions.assertThat(user.enabled).isEqualTo(newUser.enabled);
              Assertions.assertThat(user.username).isEqualTo(newUser.username);
              Assertions.assertThat(user.id).isNotNull().is(keycloakId);
            })

    ;
  }

  @Test
  public void testReadUserOk(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);
    UserRepresentation newUser = new UserRepresentation("mr", "rectangle",
        "mrrectangule@trikorasolutions.com", true,
        "mrrectangule", "mrrectangule");

    asserter
        .execute( // Delete the test user
            () -> clientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser.username))
        .execute( // Create a test user
            () -> clientLogic.createUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser))
        .assertThat(
            () -> clientLogic.getUserInfo(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser.username),
            user -> {
              Assertions.assertThat(user.firstName).isEqualTo(newUser.firstName);
              Assertions.assertThat(user.lastName).isEqualTo(newUser.lastName);
              Assertions.assertThat(user.email).is(standardEmail);
              Assertions.assertThat(user.enabled).isEqualTo(newUser.enabled);
              Assertions.assertThat(user.username).isEqualTo(newUser.username);
              Assertions.assertThat(user.id).is(keycloakId);
            })
    ;
  }

  @Test
  public void testReadUserErr(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);

    asserter
        .assertFailedWith(
            () -> clientLogic.getUserInfo(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), "nonRegisteredUser"),
            NoSuchUserException.class)
    ;
  }


  @Test
  public void testUpdateUserOk(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);
    UserRepresentation newUser = new UserRepresentation("mr", "rectangle",
        "mrrectangule@trikorasolutions.com", true,
        "mrrectangule", "mrrectangule");
    UserRepresentation updatedUser = new UserRepresentation("mr", "rectangle",
        "updatedemail@trikorasolutions.com",
        true, "mrrectangule", "mrrectangule");

    asserter
        .execute( // Delete the test user
            () -> clientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser.username))
        .execute( // Create a test user
            () -> clientLogic.createUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser))
        .assertThat(
            () -> clientLogic.updateUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), updatedUser.username, updatedUser),
            user -> Assertions.assertThat(user.email).isEqualTo(updatedUser.email))
    ;
  }

  @Test
  public void testUpdateUserErr(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);
    asserter
        .assertFailedWith(
            () -> clientLogic.updateUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(),
                "unknown", new UserRepresentation("a", "b", "c", false, "d")),
            NoSuchUserException.class)
    ;
  }

  @Test
  public void testDeleteUserOk(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);
    UserRepresentation newUser = new UserRepresentation("mr", "rectangle",
        "mrrectangule@trikorasolutions.com", true,
        "mrrectangule", "mrrectangule");

    asserter
        .execute( // Delete the test user
            () -> clientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser.username))
        .execute( // Create a test user
            () -> clientLogic.createUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser))
        .assertThat(
            () -> clientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser.username),
            bool -> Assertions.assertThat(bool).isEqualTo(true))
    ;
  }

  @Test
  public void testDeleteUserErr(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);

    asserter
        .assertThat(
            () -> clientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), "unknown"),
            bool -> Assertions.assertThat(bool).isEqualTo(false))
    ;
  }

  @Test
  public void testListKeycloakUsers(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);
    final int f = 50, m = 75;

    asserter
        .assertThat(
            () -> clientLogic.listAllUsers(
                tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId()),
            listOfUser -> {
              List<String> usernameList = listOfUser.stream()
                  .map(tuple -> tuple.username)
                  .collect(Collectors.toList());
              Assertions.assertThat(usernameList).contains("jdoe", ADM, "mrsquare", "mrtriangle");
            })
    ;

    // Test base case of recursion
    asserter
        .assertThat(
            () -> clientLogic.listAllUsers(
                tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(), f, m),
            listOfUser -> {
              Assertions.assertThat(listOfUser.size()).isEqualTo(m - f);
            })
    ;

    int m2 = 275;
    asserter
        .assertThat(
            () -> clientLogic.listAllUsers(
                tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(), f, m2),
            listOfUser -> {
              Assertions.assertThat(listOfUser.size()).isEqualTo(m2 - f);
            })
    ;

    int f3 = 0, m3 = 300;
    asserter
        .assertThat(
            () -> clientLogic.listAllUsers(
                tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(), f3, m3),
            listOfUser -> {
              Assertions.assertThat(listOfUser.size()).isEqualTo(m3 - f3);
            })
    ;
  }

  @Test
  public void testEnableDisableUser(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);
    UserRepresentation newUser = new UserRepresentation("mr", "rectangle",
        "mrrectangule@trikorasolutions.com", false,
        "mrrectangule", "mrrectangule");

    asserter
        .execute( // Delete the test user
            () -> clientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser.username))
        .execute( // Create a test user
            () -> clientLogic.createUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser))
        .assertThat(
            () -> clientLogic.enableUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser.username),
            bool -> Assertions.assertThat(bool).isEqualTo(true))
        .assertThat(
            () -> clientLogic.getUserInfo(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser.username),
            user -> Assertions.assertThat(user.enabled).isEqualTo(true))
        .assertThat(
            () -> clientLogic.disableUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser.username),
            bool -> Assertions.assertThat(bool).isEqualTo(true))
    ;

  }

  @Test
  public void testChangeUserPassword(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);
    UserRepresentation newUser = new UserRepresentation("kcpss", "kcpsslast",
        "kcpsslast@trikorasolutions.com", true,
        "kcpss", "kcpss");

    asserter
        .execute( // Delete the test user
            () -> clientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser.username))
        .execute( // Create a test user
            () -> clientLogic.createUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser))
        .assertThat(
            () -> clientLogic.resetPassword(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser.username, "1234isPerfectPassword"),
            user -> Assertions.assertThat(user).isNotNull())
    ;
  }

  @Test
  public void testCreateUserPasswordTemporal(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken(ADM, ADM);
    UserRepresentation newUser = new UserRepresentation("kcpss", "kcpsslast",
        "kcpsstmplast2@trikorasolutions.com", true,
        "kcpss2", "kcpss", true);
    asserter
        .execute( // Delete the test user
            () -> clientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser.username))
        .assertThat( // Create a test user
            () -> clientLogic.createUser(tkrKcCli.getRealmName(), accessToken,
                tkrKcCli.getClientId(), newUser),
            user -> Assertions.assertThat(user).isNotNull())
    ;
  }

}
