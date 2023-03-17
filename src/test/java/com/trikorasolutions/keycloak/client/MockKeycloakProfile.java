package com.trikorasolutions.keycloak.client;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Set;

public class MockKeycloakProfile implements QuarkusTestProfile {

  @Override
  public String getConfigProfile() {
    return "mock-keycloak";
  }

//  @Override
//  public Set<String> tags() {
//    return QuarkusTestProfile.super.tags();
//  }
}
