package com.stocat.amumal.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.profiles.include=",
      "management.server.port=0",
      "management.endpoints.web.exposure.include=health,prometheus"
    })
@ActiveProfiles("test")
class ManagementEndpointIntegrationTest {

  @Autowired private Environment environment;

  @Test
  void readinessAndMetricsAreAccessibleOnSeparateManagementPort() throws Exception {
    int managementPort = environment.getRequiredProperty("local.management.port", Integer.class);
    int applicationPort = environment.getRequiredProperty("local.server.port", Integer.class);
    HttpClient client = HttpClient.newHttpClient();

    assertThat(get(client, managementPort, "/actuator/health/readiness").statusCode()).isEqualTo(200);
    HttpResponse<String> metrics = get(client, managementPort, "/actuator/prometheus");
    assertThat(metrics.statusCode()).isEqualTo(200);
    assertThat(metrics.body()).contains("jvm_memory_used_bytes");
    assertThat(get(client, applicationPort, "/v1/actuator/prometheus").statusCode()).isNotEqualTo(200);
  }

  private HttpResponse<String> get(HttpClient client, int port, String path) throws Exception {
    return client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build(),
        HttpResponse.BodyHandlers.ofString());
  }
}
