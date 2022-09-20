package com.trikorasolutions.keycloak.client.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  @JsonProperty("members")
  public Set<KeycloakUserRepresentation> members;

  public GroupRepresentation() {}

  public GroupRepresentation(String name) {
    this.name = name;
  }

  public GroupRepresentation(String id, String name) {
    this(name);
    this.id = id;
    this.roles = new LinkedHashSet<>();
    this.members = new LinkedHashSet<>();
  }

  public GroupRepresentation(String id, String name, String path) {
    this(id, name);
    this.path = path;
  }

  public static GroupRepresentation from(JsonObject from) {
    LOGGER.debug("from(JsonObject)... {}", from);
    // All response must have a username (exception must be launched in bl)
    if (from == null || from.getString("id") == null) {
      return null;
    }

    // Create the DTO only with the mandatory fields
    GroupRepresentation parsedResponse = new GroupRepresentation(
        from.getString("id"), from.getString("name"));

    // Then add only the available optional fields (more fields will be added in future releases)
    for (String key : from.keySet()) {
      switch (key) {
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

  public Set<KeycloakUserRepresentation> getMembers() {
    return members;
  }

  public void setMembers(Set<KeycloakUserRepresentation> members) {
    this.members = members;
  }

  public GroupRepresentation addMembers(Collection<KeycloakUserRepresentation> members) {
    this.members.addAll(members);
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", GroupRepresentation.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("name='" + name + "'")
        .add("path='" + path + "'")
        .add("roles=" + roles)
        .add("members=" + members)
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
