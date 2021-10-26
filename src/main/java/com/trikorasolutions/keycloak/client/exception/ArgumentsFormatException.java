package com.trikorasolutions.keycloak.client.exception;

public class ArgumentsFormatException extends TrikoraException{
  public ArgumentsFormatException(){
    super("The arguments provided to the function are not compatible with the keycloak's standard");
  }
}
