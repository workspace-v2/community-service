package com.stocat.amumal.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.List;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

  public static final String CACHE_LIKE_COUNT = "likeCounts";
  public static final String CACHE_VIEW_COUNT = "viewCounts";
  public static final String CACHE_AUTH_TOKEN = "authTokens";

  @Bean
  public CacheManager cacheManager() {
    SimpleCacheManager cacheManager = new SimpleCacheManager();
    cacheManager.setCaches(
        List.of(
            // 좋아요 수 (만료 없음)
            buildCache(CACHE_LIKE_COUNT, Caffeine.newBuilder().maximumSize(10_000)),

            // 조회수 (만료 없음)
            buildCache(CACHE_VIEW_COUNT, Caffeine.newBuilder().maximumSize(10_000)),

            // 인증 토큰
            // TODO: 만료 지정하기
            buildCache(CACHE_AUTH_TOKEN, Caffeine.newBuilder().maximumSize(10_000))));
    return cacheManager;
  }

  // 카페인캐시 객체를 만드는 헬퍼 메서드
  private CaffeineCache buildCache(String name, Caffeine<Object, Object> caffeine) {
    return new CaffeineCache(name, caffeine.build());
  }
}
