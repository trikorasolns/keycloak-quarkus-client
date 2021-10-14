package com.trikorasolutions.keycloak.client;

import com.trikorasolutions.keycloak.client.bl.KeycloakClientLogic;
import com.trikorasolutions.keycloak.client.dto.UserRepresentation;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

@QuarkusTest
public class LogicCRUDTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(LogicCRUDTest.class);

  @Inject
  KeycloakClientLogic keycloakClientLogic;

  @Inject
  TrikoraKeycloakClientInfo tkrKcCli;

  @Test
  public void testCreateUserOk() {
    String accessToken = tkrKcCli.getAccessToken("admin");
    UserRepresentation newUser = new UserRepresentation("mr", "rectangle", "mrrectangule@trikorasolutions.com", true,
      "mrrectangule");
    JsonArray logicResponse;

    logicResponse = keycloakClientLogic.getUserInfo(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
      newUser.username).await().indefinitely(); // Ensure that the user does not exits
    assertThat(logicResponse, emptyIterable());

    logicResponse = keycloakClientLogic.createUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
      newUser).await().indefinitely(); // Create new user

    List<String> userRepresentation = logicResponse.stream().map(JsonObject.class::cast)
      .filter(tuple -> tuple.getString("username").equals(newUser.username)).map(tuple -> tuple.getString("email"))
      .collect(Collectors.toList());
    assertThat(userRepresentation.size(), is(1));
    assertThat(userRepresentation.get(0), is("mrrectangule@trikorasolutions.com"));

    keycloakClientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
      newUser.username).await().indefinitely(); // Delete the test user
  }

  @Test
  public void testCreateUserErr() {
    String accessToken = tkrKcCli.getAccessToken("admin");
    UserRepresentation newUser = new UserRepresentation("mr", "rectangle", "mrrectangule@trikorasolutions.com", true,
      "mrrectangule");
    JsonArray logicResponse;

    keycloakClientLogic.createUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
      newUser).await().indefinitely(); // Create new user

    try { // It is not possible to update users that are not registered
      keycloakClientLogic.createUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
        newUser).await().indefinitely();

      assertFalse(true);
    }catch (IllegalArgumentException e) {
      assertFalse(false);
    }finally {
      keycloakClientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
        newUser.username).await().indefinitely(); // Delete the test user
    }
  }

  @Test
  public void testReadUserOk() {
    String accessToken = tkrKcCli.getAccessToken("admin");
    UserRepresentation newUser = new UserRepresentation("mr", "rectangle", "mrrectangule@trikorasolutions.com", true,
      "mrrectangule");
    JsonArray logicResponse;

    keycloakClientLogic.createUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
      newUser).await().indefinitely(); // Create new user

    logicResponse = keycloakClientLogic.getUserInfo(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
      newUser.username).await().indefinitely(); // Gets the user info

    List<String> userEmail = logicResponse.stream().map(JsonObject.class::cast)
      .filter(tuple -> tuple.getString("username").equals(newUser.username)).map(tuple -> tuple.getString("email"))
      .collect(Collectors.toList());
    assertThat(userEmail.size(), is(1));
    assertThat(userEmail.toString(), containsString("mrrectangule@trikorasolutions.com"));

    keycloakClientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
      newUser.username).await().indefinitely(); // Delete the test user
  }

  @Test
  public void testReadUserErr() {
    String accessToken = tkrKcCli.getAccessToken("admin");
    JsonArray logicResponse;

    logicResponse = keycloakClientLogic.getUserInfo(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
      "unknown").await().indefinitely(); // Ensure that the user does not exits
    assertThat(logicResponse, emptyIterable());
  }

  @Test
  public void testUpdateUserOk() {
    String accessToken = tkrKcCli.getAccessToken("admin");
    UserRepresentation newUser = new UserRepresentation("mr", "rectangle", "mrrectangule@trikorasolutions.com", true,
      "mrrectangule");
    UserRepresentation updatedUser = new UserRepresentation("mr", "rectangle", "updatedemail@trikorasolutions.com",
      true, "mrrectangule");
    JsonArray logicResponse;

    keycloakClientLogic.createUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(), newUser).await().indefinitely();

    logicResponse = keycloakClientLogic.updateUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
      updatedUser.username, updatedUser).await().indefinitely(); // Updates the user email

    List<String> updatedRepresentation = logicResponse.stream().map(JsonObject.class::cast)
      .filter(tuple -> tuple.getString("username").equals(updatedUser.username)).map(tuple -> tuple.getString("email"))
      .collect(Collectors.toList());

    assertThat(updatedRepresentation.size(), is(1));
    assertThat(updatedRepresentation.get(0), is("updatedemail@trikorasolutions.com"));

    keycloakClientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
      newUser.username).await().indefinitely(); // Delete the test user
  }

  @Test
  public void testUpdateUserErr() {
    String accessToken = tkrKcCli.getAccessToken("admin");

    try { // It is not possible to update users that are not registered
      keycloakClientLogic.updateUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
        "unknown", new UserRepresentation("a","b","c",false,"d")).await().indefinitely();
      assertFalse(true);
    } catch (NoSuchElementException e) {
      assertFalse(false);
    }
  }

  @Test
  public void testDeleteUserOk(){
    String accessToken = tkrKcCli.getAccessToken("admin");
    UserRepresentation newUser = new UserRepresentation("mr", "rectangle", "mrrectangule@trikorasolutions.com", true,
      "mrrectangule");
    JsonArray logicResponse;

    keycloakClientLogic.createUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
      newUser).await().indefinitely(); // Create new user

    logicResponse = keycloakClientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
      newUser.username).await().indefinitely(); // Delete the test user
    assertThat(logicResponse, notNullValue());
  }

  @Test
  public void testDeleteUserErr(){
    String accessToken = tkrKcCli.getAccessToken("admin");

    try { // It is not possible to delete unregistered users
      keycloakClientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
        "unknown").await().indefinitely();
      assertFalse(true);
    }catch (NoSuchElementException e){
      assertFalse(false);
    }
  }

  @Test
  public void testListKeycloakUsers() {
    String accessToken = tkrKcCli.getAccessToken("admin");

    JsonArray logicResponse = keycloakClientLogic.listAll(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId())
      .await().indefinitely();

    List<String> usernameList = logicResponse.stream().map(JsonObject.class::cast)
      .map(tuple -> tuple.getString("username")).collect(Collectors.toList());
    assertThat(usernameList, hasItems("jdoe", "admin", "mrsquare", "mrtriangle"));
  }

}