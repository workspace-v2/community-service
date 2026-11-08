package com.stocat.amumal.search.config;

import com.stocat.amumal.search.service.ElasticsearchSearchClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/** MySQL 실행에서는 Elasticsearch 클라이언트 or 외부 연결을 만들지 않는다. */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SearchProperties.class)
public class SearchConfiguration {
  @Bean
  @ConditionalOnProperty(name = "app.search.engine", havingValue = "elasticsearch")
  ElasticsearchSearchClient elasticsearchSearchClient(
      RestClient.Builder builder, SearchProperties properties) {
    var factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(properties.connectTimeout());
    factory.setReadTimeout(properties.readTimeout());
    RestClient client =
        builder
            .baseUrl(properties.url().toString())
            .requestFactory(factory)
            .defaultHeaders(
                headers -> headers.setBasicAuth(properties.username(), properties.password()))
            .build();
    return new ElasticsearchSearchClient(client, properties);
  }
}
