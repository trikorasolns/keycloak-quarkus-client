package com.trikorasolutions.keycloak.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * This is a Download DTO, that is, it shows only the desired fields when they are requested to KC
 */
public final class KeycloakUserRepresentation {

  @JsonIgnore
  private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakUserRepresentation.class);

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

  @JsonProperty("roles")
  public Set<RoleRepresentation> roles;

  @JsonProperty("groups")
  public Set<TrikoraGroupRepresentation> groups;

  public KeycloakUserRepresentation() {
  }

  public KeycloakUserRepresentation(String username) {
    this.username = username;
    this.roles = new LinkedHashSet<>();
    this.groups = new LinkedHashSet<>();
  }

  public KeycloakUserRepresentation(String id, String firstName, String lastName, String email,
      Boolean enabled, String username) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.enabled = enabled;
    this.username = username;
    this.roles = new LinkedHashSet<>();
    this.groups = new LinkedHashSet<>();
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", KeycloakUserRepresentation.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("firstName='" + firstName + "'")
        .add("lastName='" + lastName + "'")
        .add("email='" + email + "'")
        .add("enabled=" + enabled)
        .add("username='" + username + "'")
        .add("roles=" + roles)
        .add("groups=" + groups)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof KeycloakUserRepresentation))
      return false;
    KeycloakUserRepresentation that = (KeycloakUserRepresentation) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  public static KeycloakUserRepresentation from(JsonObject from) {
    LOGGER.debug("from(JsonObject)... {}", from);
    // All response must have a username (exception must be launched in bl)
    if (from == null || from.getString("username") == null)
      return null;

    // Create the DTO only with the mandatory fields
    KeycloakUserRepresentation parsedResponse = new KeycloakUserRepresentation(
        from.getString("username"));

    // Then add only the available optional fields
    Iterator<String> iterator = from.keySet().iterator();
    while (iterator.hasNext()) {
      String key = iterator.next();
      switch (key) {
        case "id":
          parsedResponse.setId(from.getString(key));
          break;
        case "given_name":
        case "firstName":
          parsedResponse.setFirstName(from.getString(key));
          break;
        case "family_name":
        case "lastName":
          parsedResponse.setLastName(from.getString(key));
          break;
        case "email":
          parsedResponse.setEmail(from.getString(key));
          break;
        case "enabled":
          parsedResponse.setEnabled(from.getBoolean(key));
          break;
        default:
          break;
      }
    }
    //If null then set to false (uses short circuit)
    parsedResponse.enabled = parsedResponse.enabled != null && parsedResponse.enabled;
    return parsedResponse;
  }

  public static List<KeycloakUserRepresentation> allFrom(JsonArray from) {
    return from.stream().map(JsonValue::asJsonObject)
        .map(KeycloakUserRepresentation::from)
        .collect(Collectors.toList());
  }


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Set<RoleRepresentation> getRoles() {
    return roles;
  }

  public void setRoles(Set<RoleRepresentation> roles) {
    this.roles = roles;
  }

  public Set<TrikoraGroupRepresentation> getGroups() {
    return groups;
  }

  public void setGroups(Set<TrikoraGroupRepresentation> groups) {
    this.groups = groups;
  }

  public KeycloakUserRepresentation addRoles(Collection<RoleRepresentation> roles) {
    this.roles.addAll(roles);
    return this;
  }

  public KeycloakUserRepresentation addGroups(Collection<TrikoraGroupRepresentation> groups) {
    this.groups.addAll(groups);
    return this;
  }

  /**
   * Delete if not used in future versions...
   */
  @Deprecated
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
