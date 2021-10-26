package com.trikorasolutions.keycloak.client.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public class UserRepresentation {
  /**
   * In this first version of the example, the credential of the users are
   * their usernames. This feature will be enhanced in future releases.
   */
  public class UserDtoCredential {
    @JsonProperty("type")
    public String type;

    @JsonProperty("value")
    public String value;

    @JsonProperty("temporary")
    public Boolean temporary;

    public UserDtoCredential(String name){
      this.value = name;
      this.type = "password";
      this.temporary = false;
    }
  }

  @JsonProperty("firstName")
  public String firstName;

  @JsonProperty("lastName")
  public String lastName;

  @JsonProperty("email")
  public String email;

  @JsonProperty("enabled")
  public Boolean enabled;

  @JsonProperty("username")
  public String username;

  @JsonProperty("credentials")
  public Set<UserDtoCredential> credentials;

  public UserRepresentation(String firstName, String lastName, String email, Boolean enabled, String username) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.enabled = enabled;
    this.username = username;
    this.credentials = Set.of(this.new UserDtoCredential(username));
  }
}
