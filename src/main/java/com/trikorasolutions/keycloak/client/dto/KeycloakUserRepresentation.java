package com.trikorasolutions.keycloak.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.Set;

public class KeycloakUserRepresentation {
  @JsonIgnore
  private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakUserRepresentation.class);

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

    public UserDtoCredential(String name) {
      this.value = name;
      this.type = "password";
      this.temporary = false;
    }
  }

  @JsonProperty("id")
  public String id;

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

  public KeycloakUserRepresentation() {
  }

  public KeycloakUserRepresentation(String username) {
    this.username = username;
  }

  public KeycloakUserRepresentation(String id, String firstName, String lastName, String email, Boolean enabled, String username) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.enabled = enabled;
    this.username = username;
    this.credentials = Set.of(this.new UserDtoCredential(username));
  }

  @Override
  public String toString() {
    return "KeycloakUserRepresentation{" + "id='" + id + '\'' + ", firstName='" + firstName + '\'' + ", lastName='" + lastName + '\'' + ", email='" + email + '\'' + ", enabled=" + enabled + ", username='" + username + '\'' + ", credentials=" + credentials+ '}';
  }

  public static KeycloakUserRepresentation from(JsonObject from) {

    // Cannot reuse code since keycloak response fields have different keys between
    // admin and user endpoints
    if (from.containsKey("given_name")) {
      return new KeycloakUserRepresentation(from.getString("id"), from.getString("given_name"),
        from.getString("family_name"), from.getString("email"), true, from.getString("preferred_username"));
    } else if (!from.containsKey("lastName")) { // Admin user do not have a real name in this version
      return new KeycloakUserRepresentation(from.getString("id"), "IS_CONFIDENTIAL", "IS_CONFIDENTIAL",
        from.getString("email"), false, from.getString("username"));
    } else {
      return new KeycloakUserRepresentation(from.getString("id"), from.getString("firstName"),
        from.getString("lastName"), from.getString("email"), from.getBoolean("enabled"), from.getString("username"));
    }
  }

  public static KeycloakUserRepresentation from(JsonArray from) {
    // We only parse one user, so it must be stored in position with index 0
    JsonObject toParse;

    try {
      toParse = from.getJsonObject(0);
    } catch (IndexOutOfBoundsException e) {
      return new KeycloakUserRepresentation("Unknown User");
    }

    return new KeycloakUserRepresentation(toParse.getString("id"), toParse.getString("firstName"),
      toParse.getString("lastName"), toParse.getString("email"), toParse.getBoolean("enabled"),
      toParse.getString("username"));
  }

}
