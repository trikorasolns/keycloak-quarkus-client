package com.trikorasolutions.keycloak.client.exception;

public class ClientNotFoundException extends RuntimeException {

  public ClientNotFoundException(String client, String realm) {
    super("There is no client : " + client + "in realm: " + realm + "in the keycloak DB");
  }
}
