package com.stocat.amumal.post.service;

public interface PostViewService {

  void incrementViewCount(Long postId);

  long getViewCountDelta(Long postId);
}
