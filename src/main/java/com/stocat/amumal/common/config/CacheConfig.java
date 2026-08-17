package com.stocat.amumal.common.config;

import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@EnableCaching
@Profile("!test")
public class CacheConfig {

  public static final String CACHE_VIEW_COUNT = "viewCounts";
  public static final String CACHE_AUTH_TOKEN = "authTokens";

  @Bean
  public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
    return RedisCacheManager.builder(redisConnectionFactory)
        .cacheDefaults(defaultCacheConfiguration())
        // TODO: 조회수 delta 캐시 유지 주기 임시값에서 나중에 수정
        .withCacheConfiguration(CACHE_VIEW_COUNT, cacheConfiguration(Duration.ofDays(1)))
        // 인증 토큰 관련 캐시는 refresh token 수명과 비슷하게 유지
        .withCacheConfiguration(CACHE_AUTH_TOKEN, cacheConfiguration(Duration.ofDays(14)))
        .transactionAware()
        .build();
  }

  private RedisCacheConfiguration defaultCacheConfiguration() {
    return RedisCacheConfiguration.defaultCacheConfig()
        // null을 캐싱하지 않아 "데이터 없음" 상태가 오래 남는 것을 예방
        .disableCachingNullValues()
        // 캐시 값을 Spring Data Redis 4.0에서 권장되는 JSON 직렬화로 저장
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(
                GenericJacksonJsonRedisSerializer.builder().build()));
  }

  private RedisCacheConfiguration cacheConfiguration(Duration ttl) {
    // 캐시별 차이는 TTL만 두고, 나머지 정책은 공통 설정을 재사용
    return defaultCacheConfiguration().entryTtl(ttl);
  }
}
