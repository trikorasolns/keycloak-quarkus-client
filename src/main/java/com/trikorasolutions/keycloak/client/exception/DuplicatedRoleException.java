package com.trikorasolutions.keycloak.client.exception;

public class DuplicatedRoleException extends RuntimeException {
  public DuplicatedRoleException(String roleName){
    super("You are trying to create the role: " + roleName + " again in the DB");
  }
}
