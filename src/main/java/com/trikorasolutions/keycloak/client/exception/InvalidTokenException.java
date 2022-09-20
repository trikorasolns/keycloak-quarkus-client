package com.trikorasolutions.keycloak.client.exception;

public class InvalidTokenException extends RuntimeException {

  public InvalidTokenException() {
    super("Token incorrect");
  }
}
