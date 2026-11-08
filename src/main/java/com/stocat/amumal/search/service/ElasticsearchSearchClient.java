package com.stocat.amumal.search.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.menu.dto.MenuResponse;
import com.stocat.amumal.menu.service.MenuService;
import com.stocat.amumal.post.domain.ReviewTag;
import com.stocat.amumal.post.dto.PostSearchSliceResponse;
import com.stocat.amumal.post.dto.PostSearchSort;
import com.stocat.amumal.post.dto.PostSummaryResponse;
import com.stocat.amumal.search.config.SearchProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** 색인 스냅샷으로 메뉴·리뷰를 검색한다. 장애 시 MySQL로 우회하지 않고 503을 반환한다. */
public class ElasticsearchSearchClient {
  private final RestClient client;
  private final SearchProperties properties;

  public ElasticsearchSearchClient(RestClient client, SearchProperties properties) {
    this.client = client;
    this.properties = properties;
  }

  /** 분석된 텍스트 검색과 브랜드·가격 필터를 적용하고 한 건을 더 읽어 다음 페이지를 판정한다. */
  public MenuService.Slice menus(
      String keyword, Long brandId, Integer maxPrice, int offset, int limit, boolean relevance) {
    List<Object> filters = new ArrayList<>();
    if (brandId != null) filters.add(Map.of("term", Map.of("brandId", brandId)));
    if (maxPrice != null) filters.add(Map.of("range", Map.of("price", Map.of("lte", maxPrice))));
    Object text =
        keyword == null
            ? Map.of("match_all", Map.of())
            : multiMatch(keyword, List.of("name", "description", "brandName"));
    Map<String, Object> query = Map.of("bool", Map.of("must", List.of(text), "filter", filters));
    List<MenuResponse> rows =
        search(
            "menus",
            query,
            offset,
            limit,
            relevance && keyword != null,
            new ParameterizedTypeReference<SearchReply<MenuResponse>>() {});
    return new MenuService.Slice(
        rows.stream().limit(limit).toList(), offset, limit, rows.size() > limit);
  }

  /** 제목·본문·메뉴·브랜드·태그를 OR 검색하며 관련도는 BM25, 최신순은 ID 내림차순이다. */
  public PostSearchSliceResponse reviews(
      Long menuId, String keyword, int offset, int limit, PostSearchSort sort) {
    List<Object> should = new ArrayList<>();
    should.add(
        multiMatch(keyword, List.of("title", "content", "menuName", "brandName", "tagLabels")));
    List<String> tags = ReviewTag.matching(keyword).stream().map(Enum::name).toList();
    if (!tags.isEmpty()) should.add(Map.of("terms", Map.of("tags", tags)));
    List<Object> filters =
        menuId == null ? List.of() : List.of(Map.of("term", Map.of("menuId", menuId)));
    Map<String, Object> query =
        Map.of("bool", Map.of("should", should, "minimum_should_match", 1, "filter", filters));
    List<PostSummaryResponse> rows =
        search(
            "reviews",
            query,
            offset,
            limit,
            sort == PostSearchSort.RELEVANCE,
            new ParameterizedTypeReference<SearchReply<PostSummaryResponse>>() {});
    return new PostSearchSliceResponse(
        rows.stream().limit(limit).toList(), offset, limit, rows.size() > limit);
  }

  private Map<String, Object> multiMatch(String keyword, List<String> fields) {
    return Map.of("multi_match", Map.of("query", keyword, "fields", fields, "type", "best_fields"));
  }

  private <T> List<T> search(
      String kind,
      Map<String, Object> query,
      int offset,
      int limit,
      boolean relevance,
      ParameterizedTypeReference<SearchReply<T>> type) {
    List<Object> sort =
        relevance
            ? List.of(Map.of("_score", "desc"), Map.of("id", "desc"))
            : List.of(Map.of("id", "desc"));
    // ES의 shard 검색 timeout은 전체 HTTP 요청의 종료 시간을 보장하지 않는다.
    // 응답 수신에 여유를 두도록 클라이언트 readTimeout보다 짧게 설정한다.
    Map<String, Object> body =
        Map.of(
            "query",
            query,
            "from",
            offset,
            "size",
            limit + 1,
            "sort",
            sort,
            "track_total_hits",
            false,
            "timeout",
            properties.searchTimeout().toMillis() + "ms",
            "_source",
            List.of("response"));
    try {
      SearchReply<T> reply =
          client
              .post()
              .uri(
                  "/{index}/_search?allow_partial_search_results=false",
                  properties.indexPrefix() + "-" + kind)
              .contentType(MediaType.APPLICATION_JSON)
              .body(body)
              .retrieve()
              .body(type);
      if (reply == null
          || !Boolean.FALSE.equals(reply.timedOut())
          || reply.shards() == null
          || !Integer.valueOf(0).equals(reply.shards().failed())
          || reply.hits() == null
          || reply.hits().hits() == null) throw new ApiException(ErrorCode.SEARCH_UNAVAILABLE);
      List<T> rows = new ArrayList<>();
      for (Hit<T> hit : reply.hits().hits()) {
        if (hit.source() == null || hit.source().response() == null)
          throw new ApiException(ErrorCode.SEARCH_UNAVAILABLE);
        rows.add(hit.source().response());
      }
      return List.copyOf(rows);
    } catch (RestClientException exception) {
      // 인증 헤더/서버 응답을 사용자에게 노출하지 않는다. 자동 fallback은 측정 결과를 오염시킨다.
      throw new ApiException(ErrorCode.SEARCH_UNAVAILABLE);
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record SearchReply<T>(
      @JsonProperty("timed_out") Boolean timedOut,
      @JsonProperty("_shards") Shards shards,
      Hits<T> hits) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Shards(Integer failed) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Hits<T>(List<Hit<T>> hits) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Hit<T>(@JsonProperty("_source") Source<T> source) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Source<T>(T response) {}
}
