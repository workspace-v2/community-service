package com.stocat.amumal.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Profile("!test")
public class PostViewServiceImpl implements PostViewService {

  private static final String VIEW_COUNT_DELTA_KEY_PREFIX = "post:view:delta:";
  private final StringRedisTemplate stringRedisTemplate;

  @Override
  public void incrementViewCount(Long postId) {
    stringRedisTemplate.opsForValue().increment(buildDeltaKey(postId));
  }

  @Override
  public long getViewCountDelta(Long postId) {
    String value = stringRedisTemplate.opsForValue().get(buildDeltaKey(postId));
    return value == null ? 0L : Long.parseLong(value);
  }

  private String buildDeltaKey(Long postId) {
    return VIEW_COUNT_DELTA_KEY_PREFIX + postId;
  }
}
