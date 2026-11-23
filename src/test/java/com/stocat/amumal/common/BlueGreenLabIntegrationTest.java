package com.stocat.amumal.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.profiles.include=",
      "management.server.port=0",
      "jwt.secret=test-only-jwt-secret-key-should-be-overridden-32chars",
      "app.lab.revision=green-build"
    })
@ActiveProfiles({"test", "blue-green-lab"})
class BlueGreenLabIntegrationTest {

  @Autowired private Environment environment;
  @TempDir static Path faultDirectory;

  @DynamicPropertySource
  static void registerFaultPath(DynamicPropertyRegistry registry) {
    registry.add(
        "app.lab.brands.failure-file", () -> faultDirectory.resolve("fail-brands").toString());
  }

  @Test
  void failingBrandsReturnsVersionWhileReadinessRemainsHealthy() throws Exception {
    int appPort = environment.getRequiredProperty("local.server.port", Integer.class);
    int managementPort = environment.getRequiredProperty("local.management.port", Integer.class);
    try (HttpClient client = HttpClient.newHttpClient()) {
      assertThat(get(client, appPort, "/v1/brands").statusCode()).isEqualTo(200);
      Path flag = Files.createFile(faultDirectory.resolve("fail-brands"));
      var brands = get(client, appPort, "/v1/brands");
      assertThat(brands.statusCode()).isEqualTo(500);
      assertThat(brands.headers().firstValue("X-App-Revision")).hasValue("green-build");

      var readiness = get(client, managementPort, "/actuator/health/readiness");
      assertThat(readiness.statusCode()).isEqualTo(200);
      assertThat(readiness.headers().firstValue("X-App-Revision")).isEmpty();

      var metrics = get(client, managementPort, "/actuator/prometheus");
      assertThat(metrics.statusCode()).isEqualTo(200);
      assertThat(metrics.body().lines())
          .anyMatch(
              line ->
                  line.startsWith("http_server_requests_seconds_bucket{")
                      && line.contains("status=\"500\"")
                      && line.contains("uri=\"/brands\""));
      Files.delete(flag);
      assertThat(get(client, appPort, "/v1/brands").statusCode()).isEqualTo(200);
    }
  }

  private HttpResponse<String> get(HttpClient client, int port, String path) throws Exception {
    return client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build(),
        HttpResponse.BodyHandlers.ofString());
  }
}
