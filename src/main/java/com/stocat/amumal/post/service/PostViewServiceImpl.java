package com.stocat.amumal.post.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
@Profile("!test")
public class PostViewServiceImpl implements PostViewService {

  // 아직 DB에 반영되지 않은 조회수 증가량을 post 단위로 누적
  private static final String VIEW_COUNT_DELTA_KEY_PREFIX = "post:view:delta:";
  // flush 대상 post id만 따로 모아 전체 delta 키 탐색 비용을 절약
  private static final String VIEW_COUNT_DIRTY_KEY = "post:view:dirty";
  // flush가 지연돼도 키가 영구 방치되지 않도록 안전장치 TTL
  private static final long VIEW_COUNT_DELTA_TTL_SECONDS = 86_400L;
  private static final DefaultRedisScript<Long> INCREMENT_VIEW_COUNT_SCRIPT =
      new DefaultRedisScript<>(
          """
          -- delta 증가와 dirty 등록을 한 번에 처리해 flush 누락 방지
          redis.call('SADD', KEYS[2], ARGV[1])
          local delta = redis.call('INCR', KEYS[1])
          redis.call('EXPIRE', KEYS[1], ARGV[2])
          return delta
          """,
          Long.class);

  // Flush 중에도 새 조회수가 들어올 수 있으므로, delta 차감과 dirty set 정리를
  // Redis에서 원자적으로 처리해 상태 불일치 방지
  private static final DefaultRedisScript<Long> APPLY_FLUSHED_VIEW_COUNT_SCRIPT =
      new DefaultRedisScript<>(
          """
          local current = tonumber(redis.call('GET', KEYS[1]) or '0')
          local flushed = tonumber(ARGV[2])
          if current <= flushed then
            redis.call('DEL', KEYS[1])
            redis.call('SREM', KEYS[2], ARGV[1])
            return 0
          end
          redis.call('DECRBY', KEYS[1], flushed)
          redis.call('EXPIRE', KEYS[1], ARGV[3])
          redis.call('SADD', KEYS[2], ARGV[1])
          return current - flushed
          """,
          Long.class);

  private final StringRedisTemplate stringRedisTemplate;

  @Override
  public void incrementViewCount(Long postId) {
    // 여러 pod에서 동시에 호출돼도 delta 증가와 dirty 등록이 함께 반영
    stringRedisTemplate.execute(
        INCREMENT_VIEW_COUNT_SCRIPT,
        java.util.List.of(buildDeltaKey(postId), VIEW_COUNT_DIRTY_KEY),
        String.valueOf(postId),
        String.valueOf(VIEW_COUNT_DELTA_TTL_SECONDS));
  }

  @Override
  public long getViewCountDelta(Long postId) {
    String value = stringRedisTemplate.opsForValue().get(buildDeltaKey(postId));
    return value == null ? 0L : Long.parseLong(value);
  }

  @Override
  public Set<Long> getDirtyPostIds(int limit) {
    Set<Long> dirtyPostIds = new LinkedHashSet<>();
    ScanOptions scanOptions = ScanOptions.scanOptions().count(limit).build();

    // post별 delta는 처리 순서와 무관하게 독립적으로 집계되므로 정렬이 필요 없는 Set으로 구현
    try (Cursor<String> cursor =
        stringRedisTemplate.opsForSet().scan(VIEW_COUNT_DIRTY_KEY, scanOptions)) {
      while (cursor.hasNext() && dirtyPostIds.size() < limit) {
        String dirtyPostId = cursor.next();
        try {
          dirtyPostIds.add(Long.parseLong(dirtyPostId));
        } catch (NumberFormatException exception) {
          // 잘못된 값이 반복 처리되지 않도록 dirty set에서 함께 제거
          log.warn("Removing invalid post id from dirty set: {}", dirtyPostId, exception);
          stringRedisTemplate.opsForSet().remove(VIEW_COUNT_DIRTY_KEY, dirtyPostId);
        }
      }
    }

    return dirtyPostIds;
  }

  @Override
  public void applyFlushedViewCount(Long postId, long flushedDelta) {
    if (flushedDelta <= 0) {
      stringRedisTemplate.opsForSet().remove(VIEW_COUNT_DIRTY_KEY, String.valueOf(postId));
      return;
    }

    stringRedisTemplate.execute(
        APPLY_FLUSHED_VIEW_COUNT_SCRIPT,
        List.of(buildDeltaKey(postId), VIEW_COUNT_DIRTY_KEY),
        String.valueOf(postId),
        String.valueOf(flushedDelta),
        String.valueOf(VIEW_COUNT_DELTA_TTL_SECONDS));
  }

  @Override
  public Set<Long> popDirtyPostIds(int limit) {
    List<String> dirtyPostIds = stringRedisTemplate.opsForSet().pop(VIEW_COUNT_DIRTY_KEY, limit);
    if (dirtyPostIds == null || dirtyPostIds.isEmpty()) {
      return Set.of();
    }

    Set<Long> result = new LinkedHashSet<>();
    for (String dirtyPostId : dirtyPostIds) {
      try {
        result.add(Long.parseLong(dirtyPostId));
      } catch (NumberFormatException exception) {
        log.warn("Discarding invalid post id popped from dirty set: {}", dirtyPostId, exception);
      }
    }
    return result;
  }

  @Override
  public void markDirtyPost(Long postId) {
    stringRedisTemplate.opsForSet().add(VIEW_COUNT_DIRTY_KEY, String.valueOf(postId));
  }

  private String buildDeltaKey(Long postId) {
    return VIEW_COUNT_DELTA_KEY_PREFIX + postId;
  }
}
