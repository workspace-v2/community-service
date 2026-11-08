package com.stocat.amumal.search;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.stocat.amumal.menu.service.MenuService;
import com.stocat.amumal.post.dto.PostSearchSliceResponse;
import com.stocat.amumal.post.dto.PostSearchSort;
import com.stocat.amumal.post.service.PostService;
import com.stocat.amumal.post.validator.PostValidator;
import com.stocat.amumal.search.config.SearchProperties;
import com.stocat.amumal.search.service.ElasticsearchSearchClient;
import com.stocat.amumal.search.service.SearchService;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

/** 엔진 선택, 입력 검증 및 장애 시 fallback 금지를 검증한다. */
class SearchServiceTest {
  private final MenuService menus = mock(MenuService.class);
  private final PostService posts = mock(PostService.class);

  @SuppressWarnings("unchecked")
  private final ObjectProvider<ElasticsearchSearchClient> clients = mock(ObjectProvider.class);

  private SearchService service(boolean elastic) {
    return new SearchService(
        menus,
        posts,
        new PostValidator(),
        new SearchProperties(
            elastic ? SearchProperties.Engine.ELASTICSEARCH : null,
            URI.create("http://localhost:9200"),
            "reader",
            "password",
            "burger-test",
            null,
            null,
            null),
        clients);
  }

  @Test
  void defaultEngineDelegatesToUnchangedMysqlServices() {
    var service = service(false);
    var expected = new PostSearchSliceResponse(List.of(), 0, 10, false);
    when(posts.searchPosts(null, "갈릭", 0, 10, PostSearchSort.RECENT)).thenReturn(expected);
    assertThat(service.reviews(null, "갈릭", 0, 10, PostSearchSort.RECENT)).isSameAs(expected);
    service.menus("갈릭", 1L, 5000, 0, 10, true);
    verify(menus).list("갈릭", 1L, 5000, 0, 10, true);
    verifyNoInteractions(clients);
  }

  @Test
  void elasticsearchReceivesValidatedTrimmedRequestsOnly() {
    var client = mock(ElasticsearchSearchClient.class);
    when(clients.getObject()).thenReturn(client);
    var service = service(true);
    service.reviews(1L, " 갈릭 ", 0, 100, PostSearchSort.RELEVANCE);
    service.menus(" 치즈 ", null, 0, 10000, 100, false);
    verify(client).reviews(1L, "갈릭", 0, 100, PostSearchSort.RELEVANCE);
    verify(client).menus("치즈", null, 0, 10000, 100, false);
    verifyNoInteractions(menus, posts);
    assertThatThrownBy(() -> service.reviews(null, " ", 0, 10, PostSearchSort.RECENT))
        .isInstanceOf(com.stocat.amumal.common.exception.ApiException.class);
    assertThatThrownBy(() -> service.menus(null, null, null, 10001, 10, false))
        .isInstanceOf(com.stocat.amumal.common.exception.ApiException.class);
    assertThatThrownBy(() -> service.menus("x", null, null, 0, 101, false))
        .isInstanceOf(com.stocat.amumal.common.exception.ApiException.class);
  }

  @Test
  void elasticsearchFailureDoesNotFallBackToMysql() {
    var client = mock(ElasticsearchSearchClient.class);
    when(clients.getObject()).thenReturn(client);
    when(client.reviews(null, "갈릭", 0, 10, PostSearchSort.RELEVANCE))
        .thenThrow(
            new com.stocat.amumal.common.exception.ApiException(
                com.stocat.amumal.common.exception.ErrorCode.SEARCH_UNAVAILABLE));
    var service = service(true);
    assertThatThrownBy(() -> service.reviews(null, "갈릭", 0, 10, PostSearchSort.RELEVANCE))
        .isInstanceOf(com.stocat.amumal.common.exception.ApiException.class);
    verifyNoInteractions(posts, menus);
  }

  @Test
  void elasticConfigurationRequiresCredentialsAndValidPrefix() {
    assertThatThrownBy(
            () ->
                new SearchProperties(
                    SearchProperties.Engine.ELASTICSEARCH,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () -> new SearchProperties(null, null, null, null, "other-index", null, null, null))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
