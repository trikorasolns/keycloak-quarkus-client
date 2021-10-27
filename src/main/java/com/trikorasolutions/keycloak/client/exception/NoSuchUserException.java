package com.trikorasolutions.keycloak.client.exception;

public class NoSuchUserException extends RuntimeException {
  public NoSuchUserException(String userName){
    super("There is no any user with name: "+ userName + " in the Keycloak DB");
  }
}
