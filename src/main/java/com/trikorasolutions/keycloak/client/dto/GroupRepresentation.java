package com.trikorasolutions.keycloak.client.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.*;
import java.util.stream.Collectors;

public class GroupRepresentation {

  private static final Logger LOGGER = LoggerFactory.getLogger(GroupRepresentation.class);

  @JsonProperty("id")
  public String id;

  @JsonProperty("name")
  public String name;

  @JsonProperty("path")
  public String path;

  @JsonProperty("roles")
  public Set<RoleRepresentation> roles;

  public GroupRepresentation(String id) {
    this.id = id;
    this.roles = new LinkedHashSet<>();
  }

  public GroupRepresentation(String id, String name, String path) {
    this.id = id;
    this.name = name;
    this.path = path;
    this.roles = new LinkedHashSet<>();
  }

  public static GroupRepresentation from(JsonObject from) {
    LOGGER.debug("from(JsonObject)... {}", from);
    // All response must have a username (exception must be launched in bl)
    if (from == null || from.getString("id") == null) {
      return null;
    }

    // Create the DTO only with the mandatory fields
    GroupRepresentation parsedResponse = new GroupRepresentation(
        from.getString("id"));

    // Then add only the available optional fields
    Iterator<String> iterator = from.keySet().iterator();
    while (iterator.hasNext()) {
      String key = iterator.next();
      switch (key) {
        case "name":
          parsedResponse.setName(from.getString(key));
          break;
        case "path":
          parsedResponse.setPath(from.getString(key));
          break;
        default:
          break;
      }
    }
    return parsedResponse;
  }

  public static List<GroupRepresentation> allFrom(JsonArray from) {
    return from.stream().map(JsonValue::asJsonObject)
        .map(GroupRepresentation::from)
        .collect(Collectors.toList());
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Set<RoleRepresentation> getRoles() {
    return roles;
  }

  public void setRoles(Set<RoleRepresentation> roles) {
    this.roles = roles;
  }

  public GroupRepresentation addRoles(Collection<RoleRepresentation> roles) {
    this.roles.addAll(roles);
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", GroupRepresentation.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("name='" + name + "'")
        .add("path='" + path + "'")
        .add("roles=" + roles)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof GroupRepresentation)) {
      return false;
    }
    GroupRepresentation that = (GroupRepresentation) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
