package com.stocat.amumal.post.service;

import java.util.Set;

public interface PostViewService {

  void incrementViewCount(Long postId);

  long getViewCountDelta(Long postId);

  void applyFlushedViewCount(Long postId, long flushedDelta);

  /**
   * 아직 flush되지 않은 조회수 변경이 있는 게시글 ID를 최대 {@code limit}개까지 조회한다.
   *
   * @param limit 조회할 게시글 ID의 최대 개수
   * @return 조회수 반영이 필요한 게시글 ID 집합
   */
  Set<Long> getDirtyPostIds(int limit);

  /**
   * flush 작업이 처리할 게시글 ID를 최대 {@code limit}개까지 가져오면서 dirty 집합에서 제거한다.
   *
   * @param limit 가져올 게시글 ID의 최대 개수
   * @return 이번 flush가 처리할 게시글 ID 집합
   */
  Set<Long> popDirtyPostIds(int limit);

  /**
   * flush 실패 등으로 다시 처리해야 하는 게시글 ID를 dirty 집합에 등록한다.
   *
   * @param postId 다시 처리할 게시글 ID
   */
  void markDirtyPost(Long postId);
}
