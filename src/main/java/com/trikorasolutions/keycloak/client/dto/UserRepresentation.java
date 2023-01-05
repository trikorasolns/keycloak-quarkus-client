package com.trikorasolutions.keycloak.client.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;
import java.util.StringJoiner;

/**
 * This is an Upload DTO, that is, it packs all the necessary information for uploading it to KC. It
 * is important to realize that KC has it own UserRepresentation, so it is necessary to parse this
 * class before uploading.
 * <p>
 * This class is much more short than the KC UserRepresentation, that is why we are using it on
 * those first versions of our client.
 */
public final class UserRepresentation {

  @JsonIgnore
  private static final Logger LOGGER = LoggerFactory.getLogger(UserRepresentation.class);

  /**
   * In this first version of the example, the credential of the users are their usernames. This
   * feature will be enhanced in future releases.
   */
  public static class UserDtoCredential {

    @JsonProperty("type")
    public String type;

    @JsonProperty("value")
    public String value;

    @JsonProperty("temporary")
    public Boolean temporary;

    public UserDtoCredential(String type, String value, Boolean temporary) {
      this.type = type;
      this.value = value;
      this.temporary = temporary;
    }

    public UserDtoCredential(String value, Boolean isTemporary) {
      this.value = value;
      this.type = "password";
      this.temporary = isTemporary;
    }

    public UserDtoCredential(String value) {
      this(value, Boolean.FALSE);
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

  public UserRepresentation() {
  }

  public UserRepresentation(final String firstName, final String lastName, final String email,
      final Boolean enabled, final String username, final String password,
      final Boolean isTemporaryPassword) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.enabled = enabled;
    this.username = username;
    this.credentials = Set.of(new UserDtoCredential(password, isTemporaryPassword));
  }

  public UserRepresentation(final String firstName, final String lastName, final String email,
      final Boolean enabled, final String username, final String password) {
    this(firstName, lastName, email, enabled, username, password, Boolean.FALSE);
  }

  public UserRepresentation(final String firstName, final String lastName, final String email,
      final Boolean enabled, final String username) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.enabled = enabled;
    this.username = username;
    this.credentials = null;
  }

  public static UserRepresentation from(KeycloakUserRepresentation r) {
    return new UserRepresentation(r.firstName, r.lastName, r.email, r.enabled, r.username);
  }

  public static UserDtoCredential credentialsFrom(final String password, final Boolean isTemporary) {
    return new UserDtoCredential(password, isTemporary);
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

  public UserRepresentation setEnabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Set<UserDtoCredential> getCredentials() {
    return credentials;
  }

  public void setCredentials(Set<UserDtoCredential> credentials) {
    this.credentials = credentials;
  }

  public void setPassword(final String password) {
    this.credentials = Set.of(new UserDtoCredential(password));
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", UserRepresentation.class.getSimpleName() + "[", "]")
        .add("firstName='" + firstName + "'")
        .add("lastName='" + lastName + "'")
        .add("email='" + email + "'")
        .add("enabled=" + enabled)
        .add("username='" + username + "'")
        .add("credentials=" + credentials)
        .toString();
  }
}
