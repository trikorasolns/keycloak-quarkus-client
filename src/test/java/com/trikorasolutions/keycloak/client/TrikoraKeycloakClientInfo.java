package com.trikorasolutions.keycloak.client;

import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.representations.AccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

/**
 * These variables regarding the "backend-service" client belongs to a testing example project, do
 * not copy your client secret in production.
 */
@ApplicationScoped
public final class TrikoraKeycloakClientInfo {

  public static String ADM = "pm@test";
  private final Logger LOGGER = LoggerFactory.getLogger(TrikoraKeycloakClientInfo.class);

  @ConfigProperty(name = "quarkus.oidc.credentials.secret")
  private String clientSecret;

  @ConfigProperty(name = "quarkus.oidc.client-id")
  private String clientId;

  @ConfigProperty(name = "quarkus.oidc.auth-server-url")
  private String clientServerUrl;

  @ConfigProperty(name = "trikora.keycloak.realm-name")
  private String realmName;

  public String getAccessToken(String userName) {
    RestAssured.defaultParser = Parser.JSON;
    return RestAssured.given()
        .param("grant_type", "password")
        .param("username", userName)
        .param("password", userName)
        .param("client_id", clientId)
        .param("client_secret", clientSecret)
        .when()
        .post(clientServerUrl + "/protocol/openid-connect/token").as(AccessTokenResponse.class)
        .getToken();
  }

  public String getAccessToken(String userName, String password) {
    RestAssured.defaultParser = Parser.JSON;
    return RestAssured.given()
        .param("grant_type", "password")
        .param("username", userName)
        .param("password", password)
        .param("client_id", clientId)
        .param("client_secret", clientSecret)
        .when()
        .post(clientServerUrl + "/protocol/openid-connect/token").as(AccessTokenResponse.class)
        .getToken();
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public String getClientId() {
    return clientId;
  }

  public String getClientServerUrl() {
    return clientServerUrl;
  }

  public String getRealmName() {
    return realmName;
  }

}