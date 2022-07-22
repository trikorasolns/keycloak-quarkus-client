package com.trikorasolutions.keycloak.client.exception;

public class NoSuchGroupException extends RuntimeException {

  public NoSuchGroupException(String groupName) {
    super("There is no group with name " + groupName + " in the Keycloak DB");
  }
}
