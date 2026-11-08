package com.stocat.amumal.search;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.post.dto.PostSearchSort;
import com.stocat.amumal.search.config.SearchProperties;
import com.stocat.amumal.search.service.ElasticsearchSearchClient;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** HTTP 요청, 응답 역직렬화, lookahead 및 실패 응답을 검증한다. */
class ElasticsearchSearchClientTest {
  private MockRestServiceServer server;
  private ElasticsearchSearchClient client;

  @BeforeEach
  void setUp() {
    var builder = RestClient.builder().baseUrl("http://localhost:9200");
    server = MockRestServiceServer.bindTo(builder).build();
    client =
        new ElasticsearchSearchClient(
            builder.build(),
            new SearchProperties(
                SearchProperties.Engine.ELASTICSEARCH,
                URI.create("http://localhost:9200"),
                "reader",
                "password",
                "burger-test",
                null,
                null,
                null));
  }

  private String menu(long id) {
    return "{\"_source\":{\"response\":{\"id\":"
        + id
        + ",\"brandId\":1,\"brandName\":\"브랜드\",\"name\":\"치즈 버거\",\"description\":\"설명\",\"price\":5000,\"sourceType\":\"SYNTHETIC\",\"sourceUrl\":null}}}";
  }

  private String reply(String hits) {
    return "{\"took\":1,\"timed_out\":false,\"_shards\":{\"total\":1,\"successful\":1,\"failed\":0},\"hits\":{\"hits\":["
        + hits
        + "]}}";
  }

  @Test
  void menuFiltersOffsetSortAndLookahead() {
    server
        .expect(
            requestTo(
                "http://localhost:9200/burger-test-menus/_search?allow_partial_search_results=false"))
        .andExpect(jsonPath("$.timeout").value("3000ms"))
        .andExpect(jsonPath("$.from").value(10000))
        .andExpect(jsonPath("$.size").value(2))
        .andExpect(jsonPath("$.sort[0]._score").value("desc"))
        .andExpect(jsonPath("$.query.bool.filter[0].term.brandId").value(1))
        .andExpect(jsonPath("$.query.bool.filter[1].range.price.lte").value(7000))
        .andRespond(withSuccess(reply(menu(3) + "," + menu(2)), MediaType.APPLICATION_JSON));
    var result = client.menus("치즈", 1L, 7000, 10000, 1, true);
    assertThat(result.content()).extracting(r -> r.id()).containsExactly(3L);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.offset()).isEqualTo(10000);
    assertThat(result.limit()).isEqualTo(1);
    server.verify();
  }

  @Test
  void menuWithoutKeywordUsesMatchAllAndRecentSort() {
    server
        .expect(anything())
        .andExpect(jsonPath("$.query.bool.must[0].match_all").exists())
        .andExpect(jsonPath("$.sort[0].id").value("desc"))
        .andRespond(withSuccess(reply(menu(2)), MediaType.APPLICATION_JSON));
    var result = client.menus(null, null, null, 0, 10, true);
    assertThat(result.content()).hasSize(1);
    assertThat(result.hasNext()).isFalse();
    server.verify();
  }

  @Test
  void reviewTagsFilterAndDtoArePreserved() {
    String review =
        """
        {"_source":{"response":{"id":7,"menuId":2,"menuName":"치즈 버거","brandName":"브랜드","rating":4,
        "tags":["CHEESY"],"title":"후기","createdAt":"2026-11-08 10:00:00","likeCount":2,"commentCount":3,"viewCount":4,
        "author":{"userId":1,"nickname":"버거","profileImageUrl":null}}}}
        """;
    server
        .expect(anything())
        .andExpect(jsonPath("$.query.bool.minimum_should_match").value(1))
        .andExpect(jsonPath("$.query.bool.should[1].terms.tags[0]").value("CHEESY"))
        .andExpect(jsonPath("$.query.bool.filter[0].term.menuId").value(2))
        .andExpect(jsonPath("$.sort[0].id").value("desc"))
        .andRespond(withSuccess(reply(review), MediaType.APPLICATION_JSON));
    var result = client.reviews(2L, "치즈", 0, 10, PostSearchSort.RECENT);
    assertThat(result.content()).hasSize(1);
    assertThat(result.content().getFirst().author().nickname()).isEqualTo("버거");
    assertThat(result.content().getFirst().tags())
        .containsExactly(com.stocat.amumal.post.domain.ReviewTag.CHEESY);
    assertThat(result.hasNext()).isFalse();
    server.verify();
  }

  @Test
  void reviewTextOnlyHasNoTagTermsAndUsesScoreTieBreaker() {
    server
        .expect(anything())
        .andExpect(jsonPath("$.query.bool.should.length()").value(1))
        .andExpect(jsonPath("$.sort[0]._score").value("desc"))
        .andExpect(jsonPath("$.sort[1].id").value("desc"))
        .andRespond(withSuccess(reply(""), MediaType.APPLICATION_JSON));
    var result = client.reviews(null, "갈릭", 5, 10, PostSearchSort.RELEVANCE);
    assertThat(result.content()).isEmpty();
    assertThat(result.hasNext()).isFalse();
    server.verify();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "{\"timed_out\":true,\"_shards\":{\"failed\":0},\"hits\":{\"hits\":[]}}",
        "{\"timed_out\":false,\"_shards\":{\"failed\":1},\"hits\":{\"hits\":[]}}",
        "{}",
        "broken",
        "{\"timed_out\":false,\"_shards\":{\"failed\":0},\"hits\":{\"hits\":[{\"_source\":{}}]}}"
      })
  void partialTimeoutOrMalformedResponsesFailClosed(String body) {
    server.expect(anything()).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
    assertThatThrownBy(() -> client.reviews(null, "갈릭", 0, 10, PostSearchSort.RELEVANCE))
        .isInstanceOfSatisfying(
            ApiException.class,
            e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.SEARCH_UNAVAILABLE));
    server.verify();
  }

  @Test
  void missingIndexReturns503() {
    server.expect(anything()).andRespond(withStatus(HttpStatus.NOT_FOUND));
    assertThatThrownBy(() -> client.menus("갈릭", null, null, 0, 10, true))
        .isInstanceOfSatisfying(
            ApiException.class,
            e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    server.verify();
  }
}
