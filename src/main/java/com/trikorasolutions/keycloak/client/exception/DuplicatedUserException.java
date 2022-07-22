package com.trikorasolutions.keycloak.client.exception;

public class DuplicatedUserException extends RuntimeException {

  public DuplicatedUserException(String userName) {
    super("You are trying to create the user: " + userName + " again in the DB");
  }
}
