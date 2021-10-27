package com.trikorasolutions.keycloak.client.exception;

public class ArgumentsFormatException extends RuntimeException {
  public ArgumentsFormatException(String additionalMessage) {
    super("The arguments provided to the function are not compatible with the keycloak's standard\n" +
      additionalMessage);
  }
}
