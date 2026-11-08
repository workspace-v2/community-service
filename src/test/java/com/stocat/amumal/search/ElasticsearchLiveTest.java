package com.stocat.amumal.search;

import static org.assertj.core.api.Assertions.*;

import com.stocat.amumal.post.dto.PostSearchSort;
import com.stocat.amumal.search.config.SearchProperties;
import com.stocat.amumal.search.service.ElasticsearchSearchClient;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.web.client.RestClient;

/** 명시적으로 준비한 격리된 smoke 인덱스에만 실행하는 실제 Elasticsearch 호환성 검증. */
@EnabledIfEnvironmentVariable(named = "BURGER_ES_TEST_URL", matches = ".+")
class ElasticsearchLiveTest {
  @Test
  void realElasticsearchReadsDtoFiltersPaginationAndTags() throws Exception {
    var url = URI.create(System.getenv("BURGER_ES_TEST_URL"));
    String password =
        Files.readString(Path.of(System.getenv("BURGER_ES_TEST_PASSWORD_FILE"))).trim();
    var properties =
        new SearchProperties(
            SearchProperties.Engine.ELASTICSEARCH,
            url,
            "burger_search",
            password,
            "burger-feat9-smoke",
            null,
            null,
            null);
    var client =
        new ElasticsearchSearchClient(
            RestClient.builder()
                .baseUrl(url.toString())
                .defaultHeaders(h -> h.setBasicAuth("burger_search", password))
                .build(),
            properties);
    var first = client.reviews(null, "garlic", 0, 2, PostSearchSort.RECENT);
    assertThat(first.content()).extracting(r -> r.id()).containsExactly(3L, 2L);
    assertThat(first.hasNext()).isTrue();
    var next = client.reviews(null, "garlic", 2, 2, PostSearchSort.RECENT);
    assertThat(next.content()).extracting(r -> r.id()).containsExactly(1L);
    assertThat(next.hasNext()).isFalse();
    assertThat(client.reviews(1L, "garlic", 0, 10, PostSearchSort.RELEVANCE).content())
        .extracting(r -> r.id())
        .containsExactlyInAnyOrder(1L, 2L);
    assertThat(client.reviews(null, "치즈", 0, 10, PostSearchSort.RELEVANCE).content())
        .extracting(r -> r.id())
        .containsExactly(2L);
    assertThat(client.menus("garlic", 1L, 6000, 0, 10, true).content())
        .extracting(r -> r.id())
        .containsExactly(1L);
    assertThat(client.menus(null, null, null, 0, 2, false).hasNext()).isTrue();
    assertThat(client.menus("unmatchedword", null, null, 0, 10, true).content()).isEmpty();
    assertThat(client.reviews(null, "garlic", 10000, 100, PostSearchSort.RECENT).content())
        .isEmpty();
  }
}
