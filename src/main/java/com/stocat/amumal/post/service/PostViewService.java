package com.stocat.amumal.post.service;

import java.util.Set;

public interface PostViewService {

  void incrementViewCount(Long postId);

  long getViewCountDelta(Long postId);

  /**
   * 아직 flush되지 않은 조회수 변경이 있는 게시글 ID를 최대 {@code limit}개까지 조회한다.
   *
   * @param limit 조회할 게시글 ID의 최대 개수
   * @return 조회수 반영이 필요한 게시글 ID 집합
   */
  Set<Long> getDirtyPostIds(int limit);
}
