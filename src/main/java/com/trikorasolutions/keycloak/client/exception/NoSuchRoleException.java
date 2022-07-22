package com.trikorasolutions.keycloak.client.exception;

public class NoSuchRoleException extends RuntimeException {

  public NoSuchRoleException(String roleName) {
    super("There is no role with name " + roleName + " in the Keycloak DB");
  }
}
