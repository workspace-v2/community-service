package com.stocat.amumal.post.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class TestPostViewService implements PostViewService {

  private final ConcurrentHashMap<Long, AtomicLong> deltas = new ConcurrentHashMap<>();

  @Override
  public void incrementViewCount(Long postId) {
    deltas.computeIfAbsent(postId, ignored -> new AtomicLong()).incrementAndGet();
  }

  @Override
  public long getViewCountDelta(Long postId) {
    AtomicLong delta = deltas.get(postId);
    return delta == null ? 0L : delta.get();
  }
}
