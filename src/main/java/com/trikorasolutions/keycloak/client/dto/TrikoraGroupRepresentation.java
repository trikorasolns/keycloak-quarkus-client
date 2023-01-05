package com.trikorasolutions.keycloak.client.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.StringReader;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import org.keycloak.representations.idm.GroupRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TrikoraGroupRepresentation extends GroupRepresentation {

  private static final Logger LOGGER = LoggerFactory.getLogger(TrikoraGroupRepresentation.class);

  @JsonProperty("roles")
  public Set<RoleRepresentation> roles;

  @JsonProperty("members")
  public Set<KeycloakUserRepresentation> members;


  public TrikoraGroupRepresentation() {
  }

  public TrikoraGroupRepresentation(String name) {
    this.name = name;
    this.roles = new LinkedHashSet<>();
    this.members = new LinkedHashSet<>();
  }

  public TrikoraGroupRepresentation(String id, String name) {
    this(name);
    this.id = id;
  }

  public static String toJsonString(TrikoraGroupRepresentation from) {
    ObjectMapper mapper = new ObjectMapper();
    String jsonStr;
    ObjectNode node;
    try {
      // Parse java class to  json string
      jsonStr = mapper.writer().writeValueAsString(from);
    } catch (JsonProcessingException e) {
      jsonStr = null;
      e.printStackTrace();
      LOGGER.warn("Json ERROR while parsing: {}", e.getMessage());
    }
    return jsonStr;
  }

  public static JsonObject toJson(TrikoraGroupRepresentation from) {
    String jsonStr = TrikoraGroupRepresentation.toJsonString(from);
    JsonReader jsonReader = Json.createReader(new StringReader(jsonStr));
    JsonObject jsonObj = jsonReader.readObject();
    jsonReader.close();
    return jsonObj;
  }

  public static TrikoraGroupRepresentation from(JsonObject from) {
    LOGGER.debug("from(JsonObject)... {}", from);
    // All response must have a username (exception must be launched in bl)
    if (from == null || from.getString("id") == null || from.getString("name") == null) {
      return null;
    }

    // Create the DTO only with the mandatory fields
    TrikoraGroupRepresentation parsedResponse = new TrikoraGroupRepresentation(
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

  public static List<TrikoraGroupRepresentation> allFrom(JsonArray from) {
    return from.stream().map(JsonValue::asJsonObject)
        .map(TrikoraGroupRepresentation::from)
        .collect(Collectors.toList());
  }

  public Set<RoleRepresentation> getRoles() {
    return roles;
  }

  public void setRoles(Set<RoleRepresentation> roles) {
    this.roles = roles;
  }

  public TrikoraGroupRepresentation addRoles(Collection<RoleRepresentation> roles) {
    this.roles.addAll(roles);
    return this;
  }

  public Set<KeycloakUserRepresentation> getMembers() {
    return members;
  }

  public void setMembers(Set<KeycloakUserRepresentation> members) {
    this.members = members;
  }

  public TrikoraGroupRepresentation addMembers(Collection<KeycloakUserRepresentation> members) {
    this.members.addAll(members);
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", TrikoraGroupRepresentation.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("name='" + name + "'")
        .add("path='" + path + "'")
        .add("roles=" + roles)
        .add("members=" + members)
        .add("attributes=" + attributes)
        .add("realmRoles=" + realmRoles)
        .add("clientRoles=" + clientRoles)
        .add("subGroups=" + subGroups)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TrikoraGroupRepresentation)) {
      return false;
    }
    TrikoraGroupRepresentation that = (TrikoraGroupRepresentation) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
