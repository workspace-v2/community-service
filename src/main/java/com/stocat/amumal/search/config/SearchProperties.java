package com.stocat.amumal.search.config;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** 검색 엔진과 읽기 전용 Elasticsearch 연결 설정, 기본 엔진은 MySQL */
@ConfigurationProperties("app.search")
public record SearchProperties(
    Engine engine,
    URI url,
    String username,
    String password,
    String indexPrefix,
    Duration connectTimeout,
    Duration readTimeout,
    Duration searchTimeout) {
  public enum Engine {
    MYSQL,
    ELASTICSEARCH
  }

  public SearchProperties {
    engine = engine == null ? Engine.MYSQL : engine;
    indexPrefix = indexPrefix == null ? "burger-dev" : indexPrefix;
    connectTimeout = connectTimeout == null ? Duration.ofSeconds(2) : connectTimeout;
    readTimeout = readTimeout == null ? Duration.ofSeconds(5) : readTimeout;
    searchTimeout = searchTimeout == null ? Duration.ofSeconds(3) : searchTimeout;
    if (!indexPrefix.matches("burger-[a-z0-9-]+"))
      throw new IllegalArgumentException("검색 인덱스 접두사가 올바르지 않습니다.");
    if (connectTimeout.isNegative()
        || connectTimeout.isZero()
        || readTimeout.isNegative()
        || readTimeout.isZero()) throw new IllegalArgumentException("검색 연결 제한 시간은 양수여야 합니다.");
    if (searchTimeout.toMillis() < 1 || searchTimeout.compareTo(readTimeout) >= 0)
      throw new IllegalArgumentException("ES 검색 제한 시간은 1ms 이상이고 응답 대기 시간보다 짧아야 합니다.");
    if (engine == Engine.ELASTICSEARCH
        && (url == null
            || !java.util.Set.of("http", "https").contains(url.getScheme())
            || url.getHost() == null
            || url.getUserInfo() != null
            || username == null
            || username.isBlank()
            || password == null
            || password.isBlank()))
      throw new IllegalArgumentException("Elasticsearch URL과 읽기 전용 인증 정보가 필요합니다.");
  }

  /** 설정 객체가 로깅되더라도 비밀번호와 접속 인증 정보는 출력하지 않는다. */
  @Override
  public String toString() {
    return ("SearchProperties[engine=%s, indexPrefix=%s, connectTimeout=%s, "
            + "readTimeout=%s, searchTimeout=%s, password=****]")
        .formatted(engine, indexPrefix, connectTimeout, readTimeout, searchTimeout);
  }
}
