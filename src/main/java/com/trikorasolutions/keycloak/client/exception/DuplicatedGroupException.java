package com.trikorasolutions.keycloak.client.exception;

public class DuplicatedGroupException extends RuntimeException {
  public DuplicatedGroupException(String groupName){
    super("You are trying to create the group: " + groupName + " again in the DB");
  }
}
