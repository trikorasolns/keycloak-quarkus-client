package com.trikorasolutions.keycloak.client;

import com.trikorasolutions.keycloak.client.bl.KeycloakClientLogic;
import com.trikorasolutions.keycloak.client.dto.KeycloakUserRepresentation;
import com.trikorasolutions.keycloak.client.dto.UserRepresentation;
import com.trikorasolutions.keycloak.client.exception.ArgumentsFormatException;
import com.trikorasolutions.keycloak.client.exception.NoSuchUserException;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import javax.inject.Inject;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

@QuarkusTest
public class LogicCRUDTest {

  @Inject
  KeycloakClientLogic keycloakClientLogic;

  @Inject
  TrikoraKeycloakClientInfo tkrKcCli;

  @Test
  public void testCreateUserOk() {
    String accessToken = tkrKcCli.getAccessToken("admin");
    UserRepresentation newUser = new UserRepresentation("mr", "rectangle", "mrrectangule@trikorasolutions.com", true,
      "mrrectangule");
    KeycloakUserRepresentation logicResponse;

    try {
      logicResponse = keycloakClientLogic.createUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
        newUser).await().indefinitely(); // Create new user
      assertThat(logicResponse.email, is("mrrectangule@trikorasolutions.com"));

    }catch(ArgumentsFormatException | NoSuchUserException e){
      assertFalse(true);
    }finally{
      keycloakClientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
        newUser.username).await().indefinitely(); // Delete the test user
    }

  }

  @Test
  public void testCreateUserErr() {
    String accessToken = tkrKcCli.getAccessToken("admin");
    UserRepresentation newUser = new UserRepresentation("mr", "rectangle", "mrrectangule@trikorasolutions.com", true,
      "mrrectangule");
    KeycloakUserRepresentation logicResponse;

    try { // It is not possible to create the same user twice
      keycloakClientLogic.createUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
        newUser).await().indefinitely(); // Create new user
      logicResponse = keycloakClientLogic.createUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(), newUser)
        .onFailure(ArgumentsFormatException.class).recoverWithNull().await().indefinitely();

      assertThat(logicResponse, nullValue());
    }catch (ArgumentsFormatException | org.jboss.resteasy.reactive.ClientWebApplicationException e) {
      assertFalse(false);
    }catch(NoSuchUserException e) {
      assertFalse(true);
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
    KeycloakUserRepresentation logicResponse;

    try {
      keycloakClientLogic.createUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(), newUser).await().indefinitely(); // Create new user
    }catch(ArgumentsFormatException | NoSuchUserException e){
      assertFalse(true);
    }

    logicResponse = keycloakClientLogic.getUserInfo(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
      newUser.username).await().indefinitely(); // Gets the user info

    assertThat(logicResponse.firstName, is(newUser.firstName));
    assertThat(logicResponse.lastName, is(newUser.lastName));
    assertThat(logicResponse.email, is(newUser.email));
    assertThat(logicResponse.enabled, is(newUser.enabled));
    assertThat(logicResponse.username, is(newUser.username));
    assertThat(logicResponse.id, notNullValue());

    keycloakClientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
      newUser.username).await().indefinitely(); // Delete the test user
  }

//  @Test
//  public void testReadUserErr() {
//    String accessToken = tkrKcCli.getAccessToken("admin");
//    try {
//      keycloakClientLogic.getUserInfo(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
//        "unknown").await().indefinitely();
//
//      assertFalse(true);
//    }catch (NoSuchUserException e) {
//      assertFalse(false);
//    }
//  }

//  @Test
//  public void testUpdateUserOk() {
//    String accessToken = tkrKcCli.getAccessToken("admin");
//    UserRepresentation newUser = new UserRepresentation("mr", "rectangle", "mrrectangule@trikorasolutions.com", true,
//      "mrrectangule");
//    UserRepresentation updatedUser = new UserRepresentation("mr", "rectangle", "updatedemail@trikorasolutions.com",
//      true, "mrrectangule");
//    KeycloakUserRepresentation logicResponse;
//
//    keycloakClientLogic.createUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(), newUser).await().indefinitely();
//
//    logicResponse = keycloakClientLogic.updateUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
//      updatedUser.username, updatedUser).await().indefinitely(); // Updates the user email
//
//    assertThat(logicResponse.email, is(updatedUser.email));
//
//    keycloakClientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
//      newUser.username).await().indefinitely(); // Delete the test user
//  }
//
//  @Test
//  public void testUpdateUserErr() {
//    String accessToken = tkrKcCli.getAccessToken("admin");
//
//    try { // It is not possible to update users that are not registered
//      keycloakClientLogic.updateUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
//        "unknown", new UserRepresentation("a","b","c",false,"d")).await().indefinitely();
//      assertFalse(true);
//    } catch (NoSuchElementException e) {
//      assertFalse(false);
//    }
//  }
//
//  @Test
//  public void testDeleteUserOk(){
//    String accessToken = tkrKcCli.getAccessToken("admin");
//    UserRepresentation newUser = new UserRepresentation("mr", "rectangle", "mrrectangule@trikorasolutions.com", true,
//      "mrrectangule");
//    KeycloakUserRepresentation logicResponse;
//
//    keycloakClientLogic.createUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
//      newUser).await().indefinitely(); // Create new user
//
//    logicResponse = keycloakClientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
//      newUser.username).await().indefinitely(); // Delete the test user
//    assertThat(logicResponse.username, is("Unknown User"));
//  }
//
//  @Test
//  public void testDeleteUserErr(){
//    String accessToken = tkrKcCli.getAccessToken("admin");
//
//    try { // It is not possible to delete unregistered users
//      keycloakClientLogic.deleteUser(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
//        "unknown").await().indefinitely();
//      assertFalse(true);
//    }catch (NoSuchElementException e){
//      assertFalse(false);
//    }
//  }
//
//  @Test
//  public void testListKeycloakUsers() {
//    String accessToken = tkrKcCli.getAccessToken("admin");
//
//    List<KeycloakUserRepresentation> logicResponse = keycloakClientLogic.listAll(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId())
//      .await().indefinitely();
//
//    List<String> usernameList = logicResponse.stream()
//      .map(tuple -> tuple.username).collect(Collectors.toList());
//
//    assertThat(usernameList, hasItems("jdoe", "admin", "mrsquare", "mrtriangle"));
//  }

}