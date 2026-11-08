package com.stocat.amumal.search;

import static org.assertj.core.api.Assertions.*;

import com.stocat.amumal.search.config.SearchProperties;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** 설정 비밀값 마스킹과 서버·클라이언트 제한 시간의 관계를 검증한다. */
class SearchPropertiesTest {
  private SearchProperties properties(Duration searchTimeout, Duration readTimeout) {
    return new SearchProperties(
        SearchProperties.Engine.ELASTICSEARCH,
        URI.create("http://localhost:9200"),
        "reader",
        "test-secret-should-not-appear",
        "burger-test",
        null,
        readTimeout,
        searchTimeout);
  }

  @Test
  void toStringMasksPasswordWithoutChangingAuthenticationValue() {
    var properties = properties(null, null);
    assertThat(properties.toString())
        .contains("password=****", "engine=ELASTICSEARCH")
        .doesNotContain("test-secret-should-not-appear");
    assertThat(properties.password()).isEqualTo("test-secret-should-not-appear");
  }

  @Test
  void serverTimeoutDefaultsToLessThanClientReadTimeout() {
    var properties = properties(null, null);
    assertThat(properties.searchTimeout()).isEqualTo(Duration.ofSeconds(3));
    assertThat(properties.readTimeout()).isEqualTo(Duration.ofSeconds(5));
  }

  @Test
  void acceptsIndependentServerAndClientTimeouts() {
    var properties = properties(Duration.ofMillis(750), Duration.ofSeconds(2));
    assertThat(properties.searchTimeout()).isEqualTo(Duration.ofMillis(750));
    assertThat(properties.readTimeout()).isEqualTo(Duration.ofSeconds(2));
  }

  @ParameterizedTest
  @ValueSource(longs = {-1, 0, 5000, 6000})
  void rejectsNonPositiveOrNonShorterServerTimeout(long milliseconds) {
    assertThatThrownBy(() -> properties(Duration.ofMillis(milliseconds), Duration.ofSeconds(5)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejectsSubMillisecondServerTimeoutInsteadOfSendingZeroToElasticsearch() {
    assertThatThrownBy(() -> properties(Duration.ofNanos(1), Duration.ofSeconds(5)))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
