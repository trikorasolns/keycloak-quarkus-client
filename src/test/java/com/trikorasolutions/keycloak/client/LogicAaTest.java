package com.trikorasolutions.keycloak.client;

import com.trikorasolutions.keycloak.client.bl.KeycloakClientLogic;
import io.quarkus.test.junit.QuarkusTest;
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

@QuarkusTest
public class LogicAaTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(LogicAaTest.class);

  @Inject
  KeycloakClientLogic keycloakClientLogic;
  //UserRepresentation newUser = new UserRepresentation("testMan", "brave", "testMan@trikorasolutions.com", false, "testMan");

  @Inject
  TrikoraKeycloakClientInfo tkrKcCli;

  @Test
  public void testListKeycloakUsers() {
    String accessToken = tkrKcCli.getAccessToken("admin");

    JsonArray s = keycloakClientLogic.listAll(tkrKcCli.getRealmName(), accessToken, tkrKcCli.getClientId()).await()
      .indefinitely();
    //LOGGER.info("s: {}\n", s);
    //LOGGER.info("s: {}",s.stream().map(JsonObject.class::cast).map(tuple -> tuple.getString("id")).collect(Collectors.toList()));

    List<String> usernameList = s.stream().map(JsonObject.class::cast).map(tuple -> tuple.getString("username"))
      .collect(Collectors.toList());
    assertThat(usernameList, Matchers.hasItems("jdoe", "admin", "mrsquare", "mrtriangle"));

//

//    RestAssured.given().auth().oauth2(getAccessToken("admin")).when().get("/auth/admin/listUsers/trikorasolutions")
//      .then().statusCode(OK.getStatusCode())
//      .body("$.size().toString()", Matchers.greaterThanOrEqualTo("4"), "username.chars",
//        Matchers.hasItems("jdoe", "admin", "mrsquare", "mrtriangle"));
  }

//  @Test
//  public void testCRUDUsers() {
//
//    RestAssured.given().auth().oauth2(getAccessToken("admin")).when().contentType("application/json")
//      .get("/auth/admin/realms/TrikoraClientInfo.REALM/users/unknown").then().statusCode(NOT_FOUND.getStatusCode());
//
//    RestAssured.given().auth().oauth2(getAccessToken("admin")).when()
//      .body(new UserDto("mr", "rectangle", "mrrectangule@trikorasolutions.com", true, "mrrectangule"))
//      .contentType("application/json").post("/auth/admin/trikorasolutions/users").then().statusCode(OK.getStatusCode())
//      .body("email", Matchers.containsString("mrrectangule@trikorasolutions.com"));
//
//    RestAssured.given().auth().oauth2(getAccessToken("admin")).when()
//      .body(new UserDto("mr", "rectangle", "mrrectangule@trikorasolutions.com", true, "mrrectangule"))
//      .contentType("application/json").post("/auth/admin/trikorasolutions/users").then()
//      .statusCode(CONFLICT.getStatusCode());
//
//    RestAssured.given().auth().oauth2(getAccessToken("admin")).when()
//      .body(new UserDto("mr", "rectangle", "updatedmail@trikorasolutions.com", true, "mrrectangule"))
//      .contentType("application/json").put("/auth/admin/trikorasolutions/users").then().statusCode(OK.getStatusCode());
//
//    RestAssured.given().auth().oauth2(getAccessToken("admin")).when()
//      .body(new UserDto("mr", "rectangle", "updatedmail@trikorasolutions.com", true, "mrrectangule"))
//      .contentType("application/json").put("/auth/admin/trikorasolutions/unknown").then()
//      .statusCode(NOT_FOUND.getStatusCode());
//
//    RestAssured.given().auth().oauth2(getAccessToken("admin")).when().contentType("application/json")
//      .get("/auth/admin/trikorasolutions/users/mrrectangule").then().statusCode(OK.getStatusCode())
//      .body("email", Matchers.containsString("updatedmail@trikorasolutions.com"));
//
//    RestAssured.given().auth().oauth2(getAccessToken("admin")).when().contentType("application/json")
//      .delete("/auth/admin/trikorasolutions/users/mrrectangule");
//
//    RestAssured.given().auth().oauth2(getAccessToken("admin")).when().contentType("application/json")
//      .delete("/auth/admin/trikorasolutions/users/mrrectangule");
//
//  }
//
//  @Test
//  public void testGroupInfo() {
//    RestAssured.given().auth().oauth2(getAccessToken("admin")).when().contentType("application/json")
//      .get("/auth/admin/trikorasolutions/groups/tenant-tenant1").then().statusCode(OK.getStatusCode())
//      .body("$.size()", Matchers.is(1), "id.chars", Matchers.hasItem("a674d8a1-8a4d-42d5-976d-ba9c74d29433"),
//        "name.chars", Matchers.hasItem("tenant-tenant1"));
//
//    RestAssured.given().auth().oauth2(getAccessToken("admin")).when().contentType("application/json")
//      .get("/auth/admin/trikorasolutions/groups/tenant-tenant1/listUsers").then().statusCode(OK.getStatusCode())
//      .body("$.size()", Matchers.greaterThanOrEqualTo(1), "userName", Matchers.hasItem("jdoe"));
//  }
//
//  @Test
//  public void testPutUserInGroup() {
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