package com.trikorasolutions.keycloak.client.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class RoleRepresentation {

  private static final Logger LOGGER = LoggerFactory.getLogger(RoleRepresentation.class);

  @JsonProperty("id")
  public String id;

  @JsonProperty("name")
  public String name;

  @JsonProperty("description")
  public String description;

  @JsonProperty("composite")
  public Boolean composite;

  @JsonProperty("clientRole")
  public Boolean clientRole;

  @JsonProperty("containerId")
  public String containerId;

  public RoleRepresentation() {
  }

  public RoleRepresentation(String id) {
    this.id = id;
  }

  public RoleRepresentation(String name, String description) {
    this.name = name;
    this.description = description;
  }

  public RoleRepresentation(String id, String name, String description, Boolean composite,
      Boolean clientRole, String containerId) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.composite = composite;
    this.clientRole = clientRole;
    this.containerId = containerId;
  }

  public static RoleRepresentation from(JsonObject from) {
    LOGGER.debug("from(JsonObject)... {}", from);
    // All response must have a username (exception must be launched in bl)
    if (from == null || from.getString("id") == null) {
      return null;
    }

    // Create the DTO only with the mandatory fields
    RoleRepresentation parsedResponse = new RoleRepresentation(
        from.getString("id"));

    // Then add only the available optional fields
    Iterator<String> iterator = from.keySet().iterator();
    while (iterator.hasNext()) {
      String key = iterator.next();
      switch (key) {
        case "name":
          parsedResponse.setName(from.getString(key));
          break;
        case "description":
          parsedResponse.setDescription(from.getString(key));
          break;

        case "composite":
          parsedResponse.setComposite(from.getBoolean(key));
          break;
        case "clientRole":
          parsedResponse.setClientRole(from.getBoolean(key));
          break;
        case "containerId":
          parsedResponse.setContainerId(from.getString(key));
          break;
        default:
          break;
      }
    }

    return parsedResponse;
  }

  public static List<RoleRepresentation> allFrom(JsonArray from) {
    return from.stream().map(JsonValue::asJsonObject)
        .map(RoleRepresentation::from)
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Boolean getComposite() {
    return composite;
  }

  public void setComposite(Boolean composite) {
    this.composite = composite;
  }

  public Boolean getClientRole() {
    return clientRole;
  }

  public void setClientRole(Boolean clientRole) {
    this.clientRole = clientRole;
  }

  public String getContainerId() {
    return containerId;
  }

  public void setContainerId(String containerId) {
    this.containerId = containerId;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", RoleRepresentation.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("name='" + name + "'")
        .add("description='" + description + "'")
        .add("composite=" + composite)
        .add("clientRole=" + clientRole)
        .add("containerId='" + containerId + "'")
        .toString();
  }
}
