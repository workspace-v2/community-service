package com.stocat.amumal.auth.repository;

import com.stocat.amumal.auth.domain.RefreshTokenEntry;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisRefreshTokenStore implements RefreshTokenStore {

  private static final String TOKEN_KEY_PREFIX = "auth:refresh:token:";
  private static final String USER_KEY_PREFIX = "auth:refresh:user:";

  private final StringRedisTemplate redisTemplate;

  public RedisRefreshTokenStore(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public void save(RefreshTokenEntry entry) {
    deleteByUserId(entry.userId());

    Duration ttl = Duration.between(LocalDateTime.now(), entry.expiresAt());
    if (ttl.isZero() || ttl.isNegative()) {
      return;
    }

    redisTemplate.opsForValue().set(tokenKey(entry.token()), serialize(entry), ttl);
    redisTemplate.opsForValue().set(userKey(entry.userId()), entry.token(), ttl);
  }

  @Override
  public Optional<RefreshTokenEntry> findByToken(String token) {
    String raw = redisTemplate.opsForValue().get(tokenKey(token));
    if (raw == null) {
      return Optional.empty();
    }

    return deserialize(token, raw);
  }

  @Override
  public void deleteByUserId(Long userId) {
    String userKey = userKey(userId);
    String token = redisTemplate.opsForValue().get(userKey);

    if (token == null) {
      redisTemplate.delete(userKey);
      return;
    }

    redisTemplate.delete(List.of(userKey, tokenKey(token)));
  }

  @Override
  public void delete(String token) {
    Optional<RefreshTokenEntry> entry = findByToken(token);
    if (entry.isPresent()) {
      redisTemplate.delete(List.of(tokenKey(token), userKey(entry.get().userId())));
      return;
    }

    redisTemplate.delete(tokenKey(token));
  }

  private String serialize(RefreshTokenEntry entry) {
    return entry.userId() + ":" + entry.expiresAt();
  }

  private Optional<RefreshTokenEntry> deserialize(String token, String raw) {
    String[] parts = raw.split(":", 2);
    if (parts.length != 2) {
      redisTemplate.delete(tokenKey(token));
      return Optional.empty();
    }

    try {
      Long userId = Long.valueOf(parts[0]);
      LocalDateTime expiresAt = LocalDateTime.parse(parts[1]);
      return Optional.of(new RefreshTokenEntry(token, userId, expiresAt));
    } catch (NumberFormatException | DateTimeParseException exception) {
      redisTemplate.delete(tokenKey(token));
      return Optional.empty();
    }
  }

  private String tokenKey(String token) {
    return TOKEN_KEY_PREFIX + token;
  }

  private String userKey(Long userId) {
    return USER_KEY_PREFIX + userId;
  }
}
