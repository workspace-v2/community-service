package com.stocat.amumal.post.service;

import com.stocat.amumal.common.config.CacheConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PostViewServiceImpl implements PostViewService {

  private final CacheManager cacheManager;

  // 캐시에 delta +1 누적
  public void incrementViewCountCache(Long postId) {
    Cache cache = cacheManager.getCache(CacheConfig.CACHE_VIEW_COUNT);
    Cache.ValueWrapper wrapper = cache.get(postId);
    int current = wrapper != null ? (int) wrapper.get() : 0;
    cache.put(postId, current + 1);
  }
}
