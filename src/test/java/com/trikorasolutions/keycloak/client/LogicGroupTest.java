package com.trikorasolutions.keycloak.client;

import com.trikorasolutions.keycloak.client.bl.KeycloakClientLogic;
import com.trikorasolutions.keycloak.client.dto.UserRepresentation;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;


@QuarkusTest
public class LogicGroupTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(LogicGroupTest.class);

  @Inject
  KeycloakClientLogic keycloakClientLogic;

  @Inject
  TrikoraKeycloakClientInfo tkrKcCli;



  @Test
  public void testGroupInfoOk() {
    String accessToken = tkrKcCli.getAccessToken("admin");
    JsonArray logicResponse;

    logicResponse = keycloakClientLogic.getGroupInfo(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
      "tenant-tenant1").await().indefinitely();

    List<String> userRepresentation = logicResponse.stream().map(JsonObject.class::cast)
      .map(tuple -> tuple.getString("name"))
      .collect(Collectors.toList());
    assertThat(userRepresentation.size(), is(1));
    assertThat(userRepresentation, hasItem("tenant-tenant1"));
  }

  @Test
  public void testGroupInfoErr() {
    String accessToken = tkrKcCli.getAccessToken("admin");
    JsonArray logicResponse;

    logicResponse = keycloakClientLogic.getGroupInfo(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
      "unknown").await().indefinitely();

    assertThat(logicResponse, emptyIterable());
  }

  @Test
  public void testGroupListUsers() {
    String accessToken = tkrKcCli.getAccessToken("admin");
    JsonArray logicResponse;

    logicResponse = keycloakClientLogic.getUsersForGroup(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId(),
      "tenant-tenant1").await().indefinitely();

    List<String> userRepresentation = logicResponse.stream().map(JsonObject.class::cast)
      .map(tuple -> tuple.getString("username"))
      .collect(Collectors.toList());
    assertThat(userRepresentation.size(), greaterThanOrEqualTo(1));
    assertThat(userRepresentation, hasItem("jdoe"));
  }

  //@Test
//  public void testPutUserInGroup() { //LOGGER.info("ffffffffff{}",logicResponse);
//    RestAssured.given().auth().oauth2(getAccessToken("admin")).when().contentType("application/json")
//      .put("/auth/admin/trikorasolutions/users/mrsquare/groups/tenant-tenant1").then().statusCode(OK.getStatusCode())
//      .body("userName", Matchers.is("mrsquare"));
//
//    RestAssured.given().auth().oauth2(getAccessToken("admin")).when().contentType("application/json")
//      .get("/auth/admin/trikorasolutions/groups/tenant-tenant1/listUsers").then().statusCode(OK.getStatusCode())
//      .body("$.size()", Matchers.greaterThanOrEqualTo(1), "userName", Matchers.hasItem("mrsquare"));
//
//    RestAssured.given().auth().oauth2(getAccessToken("admin")).when().contentType("application/json")
//      .delete("/auth/admin/trikorasolutions/users/mrsquare/groups/tenant-tenant1").then().statusCode(OK.getStatusCode())
//      .body("userName", Matchers.is("mrsquare"));
//
//    RestAssured.given().auth().oauth2(getAccessToken("admin")).when().contentType("application/json")
//      .get("/auth/admin/trikorasolutions/groups/tenant-tenant1/listUsers").then().statusCode(OK.getStatusCode())
//      .body("$.size()", Matchers.greaterThanOrEqualTo(1), "userName", (Matchers.not(Matchers.hasItem("mrsquare"))));
//  }


}