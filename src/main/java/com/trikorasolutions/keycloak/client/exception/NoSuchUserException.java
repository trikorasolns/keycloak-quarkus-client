package com.trikorasolutions.keycloak.client.exception;

public class NoSuchUserException extends TrikoraException{
  public NoSuchUserException(){
    super("There is no such user in the Keycloak DB");
  }
}
