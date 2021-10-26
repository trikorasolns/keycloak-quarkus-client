package com.trikorasolutions.keycloak.client.exception;

public class NoSuchGroupException extends TrikoraException{
  public NoSuchGroupException(){
    super("There is no such group in the Keycloak DB");
  }
}
