package com.stocat.amumal.search;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.menu.service.MenuService;
import com.stocat.amumal.post.dto.PostSearchSliceResponse;
import com.stocat.amumal.post.dto.PostSearchSort;
import com.stocat.amumal.search.service.ElasticsearchSearchClient;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Elasticsearch 설정에서도 기존 공개 검색 URL과 응답 구조가 유지되는지 검증한다. */
@SpringBootTest(
    properties = {
      "spring.profiles.include=",
      "app.search.engine=elasticsearch",
      "app.search.url=http://127.0.0.1:1",
      "app.search.username=reader",
      "app.search.password=test-only"
    })
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ElasticsearchControllerIntegrationTest {
  @Autowired MockMvc mvc;
  @MockitoBean ElasticsearchSearchClient client;

  @BeforeEach
  void prepare() {
    when(client.menus(any(), any(), any(), anyInt(), anyInt(), anyBoolean()))
        .thenAnswer(
            call ->
                new MenuService.Slice(List.of(), call.getArgument(3), call.getArgument(4), false));
    when(client.reviews(any(), any(), anyInt(), anyInt(), any()))
        .thenAnswer(
            call ->
                new PostSearchSliceResponse(
                    List.of(), call.getArgument(2), call.getArgument(3), false));
  }

  @Test
  void combinedSearchKeepsSeparateOffsetsAndResponse() throws Exception {
    mvc.perform(
            get("/v1/search")
                .contextPath("/v1")
                .param("keyword", " garlic ")
                .param("menuOffset", "2")
                .param("reviewOffset", "5")
                .param("limit", "3"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.menus.offset").value(2))
        .andExpect(jsonPath("$.data.reviews.offset").value(5))
        .andExpect(jsonPath("$.data.reviews.hasNext").value(false));
    verify(client).menus("garlic", null, null, 2, 3, true);
    verify(client).reviews(null, "garlic", 5, 3, PostSearchSort.RELEVANCE);
  }

  @Test
  void menuAndReviewEndpointsKeepFiltersAndSort() throws Exception {
    mvc.perform(
            get("/v1/menus")
                .contextPath("/v1")
                .param("keyword", "garlic")
                .param("brandId", "1")
                .param("maxPrice", "6000"))
        .andExpect(status().isOk());
    verify(client).menus("garlic", 1L, 6000, 0, 20, false);
    mvc.perform(
            get("/v1/posts/search")
                .contextPath("/v1")
                .param("keyword", "garlic")
                .param("menuId", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content").isArray());
    verify(client).reviews(2L, "garlic", 0, 10, PostSearchSort.RECENT);
  }

  @Test
  void invalidInputsAreRejectedBeforeSearch() throws Exception {
    mvc.perform(get("/v1/posts/search").contextPath("/v1").param("keyword", " "))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/v1/menus").contextPath("/v1").param("offset", "10001"))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(client);
  }

  @Test
  void unavailableSearchIs503() throws Exception {
    when(client.reviews(null, "garlic", 0, 10, PostSearchSort.RECENT))
        .thenThrow(new ApiException(ErrorCode.SEARCH_UNAVAILABLE));
    mvc.perform(get("/v1/posts/search").contextPath("/v1").param("keyword", "garlic"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.code").value("SEARCH_UNAVAILABLE"));
  }
}
