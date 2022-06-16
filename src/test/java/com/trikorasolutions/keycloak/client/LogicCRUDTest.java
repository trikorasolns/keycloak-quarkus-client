package com.trikorasolutions.keycloak.client;

import com.trikorasolutions.keycloak.client.bl.KeycloakClientLogic;
import com.trikorasolutions.keycloak.client.dto.KeycloakUserRepresentation;
import com.trikorasolutions.keycloak.client.dto.UserRepresentation;
import com.trikorasolutions.keycloak.client.exception.*;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class LogicCRUDTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogicCRUDTest.class);

  @Inject
  KeycloakClientLogic keycloakClientLogic;

  @Inject
  TrikoraKeycloakClientInfo tkrKcCli;

  @Test
  public void testCreateUserOk() {
    String accessToken = tkrKcCli.getAccessToken(ADM);
    UserRepresentation newUser = new UserRepresentation("mr", "rectangle",
        "mrrectangule@trikorasolutions.com", true,
        "mrrectangule");
    KeycloakUserRepresentation logicResponse;

    keycloakClientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
            newUser.username).onFailure(NoSuchUserException.class).recoverWithNull().await()
        .indefinitely(); // Delete the test user

    logicResponse = keycloakClientLogic.createUser(tkrKcCli.getRealmName(), accessToken,
        tkrKcCli.getClientId(), newUser).await().indefinitely();// Create new user

    assertThat(logicResponse.email, is("mrrectangule@trikorasolutions.com"));
  }

  @Test
  public void testCreateUserDuplicatedErr() {
    String accessToken = tkrKcCli.getAccessToken(ADM);
    UserRepresentation newUser = new UserRepresentation("mr", "rectangle",
        "mrrectangule@trikorasolutions.com", true,
        "mrrectangule");

    keycloakClientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
            newUser.username).onFailure(NoSuchUserException.class).recoverWithNull().await()
        .indefinitely(); // Delete the test user

    keycloakClientLogic.createUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
        newUser).await().indefinitely(); // Create new user

    try {
      keycloakClientLogic.createUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
              newUser)
          .onFailure(DuplicatedUserException.class).transform(x -> {
            throw (DuplicatedUserException) x;
          }).await().indefinitely();

      assertTrue(false);
    } catch (DuplicatedUserException ex) {
      assertThat(ex.getClass(), is(DuplicatedUserException.class));
      assertThat(ex.getMessage(), containsString(newUser.username));
    }
  }

  @Test
  public void testCreateUserInvalidTokenErr() {
    String accessToken = tkrKcCli.getAccessToken(ADM);
    UserRepresentation newUser = new UserRepresentation("mr", "rectangle",
        "mrrectangule@trikorasolutions.com", true,
        "mrrectangule");

    keycloakClientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
            newUser.username).onFailure(NoSuchUserException.class).recoverWithNull().await()
        .indefinitely(); // Delete the test user
    accessToken = "bad token";
    try {
      keycloakClientLogic.createUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
              newUser)
          .onFailure(InvalidTokenException.class).transform(x -> {
            throw (InvalidTokenException) x;
          }).await().indefinitely();

      assertTrue(false);
    } catch (InvalidTokenException ex) {
      assertThat(ex.getClass(), is(InvalidTokenException.class));
    }
  }

  @Test
  public void testCreateUserNotFoundErr() {
    String accessToken = tkrKcCli.getAccessToken(ADM);
    UserRepresentation newUser = new UserRepresentation("mr", "rectangle",
        "mrrectangule@trikorasolutions.com", true,
        "mrrectangule");

    keycloakClientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
            newUser.username).onFailure(NoSuchUserException.class).recoverWithNull().await()
        .indefinitely(); // Delete the test user

    try {
      keycloakClientLogic.createUser("realm_is_not_defined", accessToken, "client_is_not_defined",
              newUser)
          .onFailure(ClientNotFoundException.class).transform(x -> {
            throw (ClientNotFoundException) x;
          }).await().indefinitely();

      assertTrue(false);
    } catch (ClientNotFoundException ex) {
      assertThat(ex.getClass(), is(ClientNotFoundException.class));
      assertThat(ex.getMessage(), containsString("client_is_not_defined"));
    }
  }

  @Test
  public void testCreateUserInvalidUserErr() {
    String accessToken = tkrKcCli.getAccessToken(ADM);
    UserRepresentation newUser = new UserRepresentation(null, null, null, true, null);

    keycloakClientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
            newUser.username).onFailure(NoSuchUserException.class).recoverWithNull().await()
        .indefinitely(); // Delete the test user

    try {
      keycloakClientLogic.createUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
              newUser)
          .onFailure(ArgumentsFormatException.class).transform(x -> {
            throw (ArgumentsFormatException) x;
          }).await().indefinitely();

      assertTrue(false);
    } catch (ArgumentsFormatException ex) {
      assertThat(ex.getClass(), is(ArgumentsFormatException.class));
    }
  }

  @Test
  public void testCreateWithOutEmailOk() {
    String accessToken = tkrKcCli.getAccessToken(ADM);
    UserRepresentation newUser = new UserRepresentation("mr", "rectangle", null, true,
        "mrrectangule");
    KeycloakUserRepresentation logicResponse;

    keycloakClientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
            newUser.username).onFailure(NoSuchUserException.class).recoverWithNull().await()
        .indefinitely(); // Delete the test user

    keycloakClientLogic.createUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
        newUser).await().indefinitely(); // Create new user

    logicResponse = keycloakClientLogic.getUserInfo(tkrKcCli.getRealmName(), accessToken,
        tkrKcCli.getClientId(),
        newUser.username).await().indefinitely(); // Gets the user info

    assertThat(logicResponse.firstName, is(newUser.firstName));
    assertThat(logicResponse.lastName, is(newUser.lastName));
    assertThat(logicResponse.email, is(nullValue()));
    assertThat(logicResponse.enabled, is(newUser.enabled));
    assertThat(logicResponse.username, is(newUser.username));
    assertThat(logicResponse.id, notNullValue());
  }

  @Test
  public void testReadUserOk() {
    String accessToken = tkrKcCli.getAccessToken(ADM);
    UserRepresentation newUser = new UserRepresentation("mr", "rectangle",
        "mrrectangule@trikorasolutions.com", true,
        "mrrectangule");
    KeycloakUserRepresentation logicResponse;

    keycloakClientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
            newUser.username).onFailure(NoSuchUserException.class).recoverWithNull().await()
        .indefinitely(); // Delete the test user

    keycloakClientLogic.createUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
        newUser).await().indefinitely(); // Create new user

    logicResponse = keycloakClientLogic.getUserInfo(tkrKcCli.getRealmName(), accessToken,
        tkrKcCli.getClientId(),
        newUser.username).await().indefinitely(); // Gets the user info

    LOGGER.info("GET USER INFO: {}", logicResponse);

    assertThat(logicResponse.firstName, is(newUser.firstName));
    assertThat(logicResponse.lastName, is(newUser.lastName));
    assertThat(logicResponse.email, is(newUser.email));
    assertThat(logicResponse.enabled, is(newUser.enabled));
    assertThat(logicResponse.username, is(newUser.username));
    assertThat(logicResponse.id, notNullValue());
  }

  @Test
  public void testReadUserErr() {
    String accessToken = tkrKcCli.getAccessToken(ADM);

    try {
      keycloakClientLogic.getUserInfo(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
          "unknown").onFailure(NoSuchUserException.class).transform(x -> {
        throw (NoSuchUserException) x;
      }).await().indefinitely();

      assertTrue(false);
    } catch (NoSuchUserException ex) {
      assertThat(ex.getClass(), is(NoSuchUserException.class));
      assertThat(ex.getMessage(), containsString("unknown"));
    }
  }


  @Test
  public void testUpdateUserOk() {
    String accessToken = tkrKcCli.getAccessToken(ADM);
    UserRepresentation newUser = new UserRepresentation("mr", "rectangle",
        "mrrectangule@trikorasolutions.com", true,
        "mrrectangule");
    UserRepresentation updatedUser = new UserRepresentation("mr", "rectangle",
        "updatedemail@trikorasolutions.com",
        true, "mrrectangule");
    KeycloakUserRepresentation logicResponse;

    keycloakClientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
            newUser.username).onFailure(NoSuchUserException.class).recoverWithNull().await()
        .indefinitely(); // Delete the test user

    keycloakClientLogic.createUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
        newUser).await().indefinitely();

    logicResponse = keycloakClientLogic.updateUser(tkrKcCli.getRealmName(), accessToken,
        tkrKcCli.getClientId(),
        updatedUser.username, updatedUser).await().indefinitely(); // Updates the user email

    assertThat(logicResponse.email, is(updatedUser.email));

  }

  @Test
  public void testUpdateUserErr() {
    String accessToken = tkrKcCli.getAccessToken(ADM);

    try { // It is not possible to update users that are not registered
      keycloakClientLogic.updateUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
              "unknown", new UserRepresentation("a", "b", "c", false, "d"))
          .onFailure(NoSuchUserException.class).transform(x -> {
            throw (NoSuchUserException) x;
          }).await().indefinitely();

      assertTrue(false);
    } catch (NoSuchUserException ex) {
      assertThat(ex.getClass(), is(NoSuchUserException.class));
      assertThat(ex.getMessage(), containsString("unknown"));
    }
  }

  @Test
  public void testDeleteUserOk() {
    String accessToken = tkrKcCli.getAccessToken(ADM);
    UserRepresentation newUser = new UserRepresentation("mr", "rectangle",
        "mrrectangule@trikorasolutions.com", true,
        "mrrectangule");
    Boolean logicResponse;

    keycloakClientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
            newUser.username).onFailure(NoSuchUserException.class).recoverWithNull().await()
        .indefinitely(); // Delete the test user

    keycloakClientLogic.createUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
        newUser).await().indefinitely(); // Create new user

    logicResponse = keycloakClientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken,
        tkrKcCli.getClientId(),
        newUser.username).await().indefinitely(); // Delete the test user

    assertTrue(logicResponse);
  }

  @Test
  public void testDeleteUserErr() {
    String accessToken = tkrKcCli.getAccessToken(ADM);

    try { // It is not possible to delete unregistered users
      keycloakClientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
          "unknown").onFailure(NoSuchUserException.class).transform(x -> {
        throw (NoSuchUserException) x;
      }).await().indefinitely();

      assertTrue(false);
    } catch (NoSuchUserException ex) {
      assertThat(ex.getClass(), is(NoSuchUserException.class));
      assertThat(ex.getMessage(), containsString("unknown"));
    }
  }

  @Test
  public void testListKeycloakUsers() {
    String accessToken = tkrKcCli.getAccessToken(ADM);

    List<KeycloakUserRepresentation> logicResponse = keycloakClientLogic.listAllUsers(
            tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId())
        .await().indefinitely();

    List<String> usernameList = logicResponse.stream()
        .map(tuple -> tuple.username).collect(Collectors.toList());

    assertThat(usernameList, hasItems("jdoe", ADM, "mrsquare", "mrtriangle"));
  }

  @Test
  public void testEnableDisableUser() {
    String accessToken = tkrKcCli.getAccessToken(ADM);
    UserRepresentation newUser = new UserRepresentation("mr", "rectangle",
        "mrrectangule@trikorasolutions.com", false,
        "mrrectangule");
    KeycloakUserRepresentation logicResponse;
    boolean logicResponse2;
    // Creates the user
    keycloakClientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
            newUser.username).onFailure(NoSuchUserException.class).recoverWithNull().await()
        .indefinitely(); // Delete the test user
    logicResponse = keycloakClientLogic.createUser(tkrKcCli.getRealmName(), accessToken,
        tkrKcCli.getClientId(), newUser).await().indefinitely();
    assertThat(logicResponse.email, is("mrrectangule@trikorasolutions.com"));

    // Enable the user
    logicResponse2 = keycloakClientLogic.enableUser(tkrKcCli.getRealmName(), accessToken,
        tkrKcCli.getClientId(), newUser.username).await().indefinitely();
    assertThat(logicResponse2, is(true));

    // Disable the user
    logicResponse2 = keycloakClientLogic.disableUser(tkrKcCli.getRealmName(), accessToken,
        tkrKcCli.getClientId(), newUser.username).await().indefinitely();
    assertThat(logicResponse2, is(true));
  }
}
