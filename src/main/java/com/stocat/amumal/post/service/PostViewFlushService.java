package com.stocat.amumal.post.service;

import com.stocat.amumal.post.repository.PostRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostViewFlushService {

  private final PostViewService postViewService;
  private final PostRepository postRepository;
  private final TransactionTemplate transactionTemplate;

  public int flushDirtyPostViewCounts(int limit) {
    Set<Long> dirtyPostIds = postViewService.popDirtyPostIds(limit);
    int flushedPostCount = 0;

    for (Long postId : dirtyPostIds) {
      try {
        flushedPostCount += flushSinglePost(postId);
      } catch (RuntimeException exception) {
        postViewService.markDirtyPost(postId);
        log.warn("Failed to flush view-count delta: postId={}", postId, exception);
      }
    }

    return flushedPostCount;
  }

  private int flushSinglePost(Long postId) {
    long delta = postViewService.getViewCountDelta(postId);
    if (delta <= 0) {
      // Flush 대기 중 다른 요청이 dirty marker를 다시 세울 수 있어 0 이하 delta에서는 건드리지 않는다.
      return 0;
    }

    Integer updatedCount =
        transactionTemplate.execute(
            transactionStatus -> {
              int count = postRepository.incrementViewCount(postId, delta);
              registerFlushSynchronization(postId, delta);
              return count;
            });

    if (updatedCount == null || updatedCount == 0) {
      log.info("Dropping view-count delta for deleted post: postId={}, delta={}", postId, delta);
      return 0;
    }

    return 1;
  }

  private void registerFlushSynchronization(Long postId, long delta) {
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            // DB commit이 확정된 뒤에만 Redis delta를 차감해 조회수 유실 방지
            postViewService.applyFlushedViewCount(postId, delta);
          }

          @Override
          public void afterCompletion(int status) {
            if (status != STATUS_COMMITTED) {
              postViewService.markDirtyPost(postId);
            }
          }
        });
  }
}
