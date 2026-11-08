package com.stocat.amumal.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockingDetails;

import com.stocat.amumal.search.service.ElasticsearchSearchClient;
import com.stocat.amumal.search.service.SearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

/** mock 대체 없이 ES 모드의 실제 클라이언트 생성과 앱 시작 의존성을 검증한다. */
@SpringBootTest(
    properties = {
      "spring.profiles.include=", "app.search.engine=elasticsearch",
      "app.search.url=http://127.0.0.1:1", "app.search.username=reader",
      "app.search.password=startup-test-only"
    })
@ActiveProfiles("test")
class ElasticsearchStartupIntegrationTest {
  @Autowired RestClient.Builder builder;
  @Autowired ElasticsearchSearchClient client;
  @Autowired SearchService searchService;

  @Test
  void startsWithRealClientWithoutContactingElasticsearch() {
    assertThat(builder).isNotNull();
    assertThat(client).isNotNull();
    assertThat(searchService).isNotNull();
    assertThat(mockingDetails(client).isMock()).isFalse();
  }
}
