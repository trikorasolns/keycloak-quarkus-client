package com.trikorasolutions.keycloak.client.it;

import com.trikorasolutions.keycloak.client.clientresource.KeycloakAuthAdminResource;
import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

public class KeycloakResource implements QuarkusTestResourceLifecycleManager,
    DevServicesContext.ContextAware {

  private Optional<String> containerNetworkId;
  private GenericContainer container;

  KeycloakAuthAdminResource keycloakClient;

  @Override
  public void setIntegrationTestContext(DevServicesContext context) {
    containerNetworkId = context.containerNetworkId();
  }

  @Override
  public Map<String, String> start() {
    Map<String, String> keycloakContainerConfiguration = new HashMap<>() {
      {
        put("KEYCLOAK_ADMIN", "admin");
        put("KEYCLOAK_ADMIN_PASSWORD", "admin");
        put("KEYCLOAK_USER", "admin"); // version 15
        put("KEYCLOAK_PASSWORD", "admin"); // version 15
        put("KEYCLOAK_IMPORT", "/tmp/trikora-realm.json");
        put("JAVA_OPTS_APPEND", "-Dkeycloak.profile.feature.upload_scripts=enabled");
      }
    };
//    8009, 8443, 9990
    // start a container making sure to call withNetworkMode() with the value of containerNetworkId if present
    container = new GenericContainer(
        DockerImageName.parse("quay.io/keycloak/keycloak:15.0.2"))
        .withCopyFileToContainer(
            MountableFile.forClasspathResource("trikora-realm.json"), "/tmp/trikora-realm.json")
        .withEnv(
            keycloakContainerConfiguration).withExposedPorts(8080)
        .waitingFor(Wait.forHttp("/")).withLogConsumer(outputFrame -> {
        });

    // apply the network to the container
    containerNetworkId.ifPresent(container::withNetworkMode);

    // start container before retrieving its URL or other properties
    container.start();
    // return a map containing the configuration the application needs to use the service
    Map<String, String> quarkusEnvironment = new HashMap<String, String>() {
      {
        put("quarkus.oidc.auth-server-url",
            String.format("http://localhost:%s/auth/realms/trikorasolutions",
                container.getMappedPort(8080)));
        put("quarkus.oidc.client-id", "backend-service");
        put("quarkus.oidc.credentials.secret", "6e521ebe-e300-450f-811a-a08adc42ec4a");
        put("quarkus.oidc.tls.verification", "none");
        put("quarkus.http.cors", "true");
        put("keycloak-api/mp-rest/url", String.format("http://localhost:%s/",
            container.getMappedPort(8080)));
        put("keycloak-api/mp-rest/scope", "javax.inject.Singleton");
      }
    };
    return ImmutableMap.copyOf(quarkusEnvironment);
  }

  @Override
  public void stop() {
    container.stop();
    // close container
  }

}