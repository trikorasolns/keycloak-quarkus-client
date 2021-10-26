package com.trikorasolutions.keycloak.client;

import com.trikorasolutions.keycloak.client.bl.KeycloakClientLogic;
import com.trikorasolutions.keycloak.client.dto.KeycloakUserRepresentation;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

//@QuarkusTest
//public class LogicGroupTest {
//
//  @Inject
//  KeycloakClientLogic keycloakClientLogic;
//
//  @Inject
//  TrikoraKeycloakClientInfo tkrKcCli;
//
//  @Test
//  public void testGroupInfoOk() {
//    String accessToken = tkrKcCli.getAccessToken("admin");
//    JsonArray logicResponse;
//
//    logicResponse = keycloakClientLogic.getGroupInfo(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
//      "tenant-tenant1").await().indefinitely();
//
//    List<String> userRepresentation = logicResponse.stream().map(JsonObject.class::cast)
//      .map(tuple -> tuple.getString("name"))
//      .collect(Collectors.toList());
//    assertThat(userRepresentation.size(), is(1));
//    assertThat(userRepresentation, hasItem("tenant-tenant1"));
//  }
//
//  @Test
//  public void testGroupInfoErr() {
//    String accessToken = tkrKcCli.getAccessToken("admin");
//    JsonArray logicResponse;
//
//    logicResponse = keycloakClientLogic.getGroupInfo(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
//      "unknown").await().indefinitely();
//    assertThat(logicResponse, emptyIterable());
//  }
//
//  @Test
//  public void testGroupListUsers() {
//    String accessToken = tkrKcCli.getAccessToken("admin");
//    List<KeycloakUserRepresentation> logicResponse;
//
//    logicResponse = keycloakClientLogic.getUsersForGroup(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
//      "tenant-tenant1").await().indefinitely();
//
//    List<String> userRepresentation = logicResponse.stream()
//      .map(user -> user.username)
//      .collect(Collectors.toList());
//    assertThat(userRepresentation.size(), greaterThanOrEqualTo(1));
//    assertThat(userRepresentation, hasItem("jdoe"));
//  }
//
//  @Test
//  public void testPutAndRemoveUserInGroup() {
//    String accessToken = tkrKcCli.getAccessToken("admin");
//    KeycloakUserRepresentation logicResponse;
//    List<KeycloakUserRepresentation> logicResponse2;
//
//    // Put a new user in the group
//    logicResponse = keycloakClientLogic.putUserInGroup(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
//      "mrsquare", "tenant-tenant1").await().indefinitely();
//
//    assertThat(logicResponse.username, is("mrsquare"));
//
//    // Check if the change has been persisted in keycloak
//    logicResponse2 = keycloakClientLogic.getUsersForGroup(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
//      "tenant-tenant1").await().indefinitely();
//    List<String> userRepresentation = logicResponse2.stream()
//      .map(user -> user.username)
//      .collect(Collectors.toList());
//    assertThat(userRepresentation.size(), greaterThanOrEqualTo(1));
//    assertThat(userRepresentation, hasItem("mrsquare"));
//
//    // Kick the user out of the group
//    logicResponse = keycloakClientLogic.deleteUserFromGroup(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
//      "mrsquare", "tenant-tenant1").await().indefinitely();
//    assertThat(logicResponse.username, is("mrsquare"));
//
//    // Check if the change has been persisted in keycloak
//    logicResponse2 = keycloakClientLogic.getUsersForGroup(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
//      "tenant-tenant1").await().indefinitely();
//    userRepresentation = logicResponse2.stream()
//      .map(user -> user.username)
//      .collect(Collectors.toList());
//    assertThat(userRepresentation.size(), greaterThanOrEqualTo(0));
//    assertThat(userRepresentation, not(hasItem("mrsquare")));
//  }
//}