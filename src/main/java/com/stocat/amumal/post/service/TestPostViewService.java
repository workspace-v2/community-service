package com.stocat.amumal.post.service;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class TestPostViewService implements PostViewService {

  private final ConcurrentHashMap<Long, AtomicLong> deltas = new ConcurrentHashMap<>();
  private final ConcurrentSkipListSet<Long> dirtyPostIds = new ConcurrentSkipListSet<>();

  @Override
  public void incrementViewCount(Long postId) {
    deltas.computeIfAbsent(postId, ignored -> new AtomicLong()).incrementAndGet();
    dirtyPostIds.add(postId);
  }

  @Override
  public long getViewCountDelta(Long postId) {
    AtomicLong delta = deltas.get(postId);
    return delta == null ? 0L : delta.get();
  }

  @Override
  public Set<Long> getDirtyPostIds(int limit) {
    Set<Long> result = new LinkedHashSet<>();
    for (Long dirtyPostId : dirtyPostIds) {
      result.add(dirtyPostId);
      if (result.size() == limit) {
        break;
      }
    }
    return result;
  }
}
