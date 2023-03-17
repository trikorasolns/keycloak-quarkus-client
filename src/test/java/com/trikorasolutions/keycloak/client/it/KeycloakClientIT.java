package com.trikorasolutions.keycloak.client.it;

import com.trikorasolutions.keycloak.client.bl.KeycloakClientLogic;
import com.trikorasolutions.keycloak.client.dto.UserRepresentation;
import io.quarkus.test.TestReactiveTransaction;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.vertx.UniAsserter;
import javax.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@QuarkusIntegrationTest
@QuarkusTestResource(KeycloakResource.class)
@TestReactiveTransaction
//@TestInstance(Lifecycle.PER_CLASS)
public class KeycloakClientIT {

  @Inject
  KeycloakClientLogic clientLogic;

  @Inject
  TrikoraKeycloakClientInfo tkrKcCli;

//  @InjectMock
//  @RestClient
//  KeycloakAuthorizationResource keycloakUserClient;

//  @Inject
//  KeycloakClientLogic clientLogic;

//  @Inject
//  TrikoraKeycloakClientInfo tkrKcCli;
//  private GenericContainer keycloakContainer;

//  @BeforeAll
//  public void setup() {
//    keycloakContainer = new GenericContainer(
//        DockerImageName.parse("quay.io/keycloak/keycloak:15.0.2")).withExposedPorts(8090)
//        .waitingFor(Wait.forHttp("/"));
//    keycloakContainer.start();
//    while (!keycloakContainer.isHostAccessible()) {
//
//    }
//  }

//  @AfterAll
//  public void cleanup() {
//    keycloakContainer.stop();
//    keycloakContainer.close();
//  }


  @Test
  @Order(1)
  public void testCreateUserOk(UniAsserter asserter) {
    final String accessToken = tkrKcCli.getAccessToken("admin", "admin");
    final UserRepresentation newUser = new UserRepresentation("test", "create",
        "testcreate@trikorasolutions.com", true,
        "testcreate", "testcreate");
    asserter
        .execute( // Delete the test user
            () -> clientLogic.deleteUser("trikorasolutions", accessToken,
                tkrKcCli.getClientId(), newUser.username))
        .assertThat( // Create a test user
            () -> clientLogic.createUser("trikorasolutions", accessToken,
                tkrKcCli.getClientId(), newUser),
            user -> {
              Assertions.assertThat(user.username).isEqualTo(newUser.username);
              Assertions.assertThat(user.email).isEqualTo(newUser.email);
            })
    ;
  }
}