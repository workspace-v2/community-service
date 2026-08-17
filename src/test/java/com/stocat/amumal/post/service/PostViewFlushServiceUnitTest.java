package com.stocat.amumal.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.stocat.amumal.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class PostViewFlushServiceUnitTest {

  @Mock private PostRepository postRepository;
  @Mock private TransactionTemplate transactionTemplate;

  private TestPostViewService postViewService;
  private PostViewFlushService postViewFlushService;

  @BeforeEach
  void setUp() {
    postViewService = new TestPostViewService();
    postViewFlushService =
        new PostViewFlushService(postViewService, postRepository, transactionTemplate);

    lenient()
        .doAnswer(invocation -> executeWithTransactionSynchronization(invocation.getArgument(0)))
        .when(transactionTemplate)
        .execute(any());
  }

  @Test
  @DisplayName("배치 중 일부 post flush가 실패해도 나머지 post는 계속 처리한다")
  void flushDirtyPostViewCountsContinuesAfterSinglePostFailure() {
    postViewService.incrementViewCount(1L);
    postViewService.incrementViewCount(2L);
    postViewService.incrementViewCount(3L);

    when(postRepository.incrementViewCount(1L, 1L)).thenReturn(1);
    when(postRepository.incrementViewCount(2L, 1L)).thenThrow(new RuntimeException("db failure"));
    when(postRepository.incrementViewCount(3L, 1L)).thenReturn(1);

    int flushedPostCount = postViewFlushService.flushDirtyPostViewCounts(10);

    assertThat(flushedPostCount).isEqualTo(2);
    assertThat(postViewService.getViewCountDelta(1L)).isZero();
    assertThat(postViewService.getViewCountDelta(2L)).isEqualTo(1L);
    assertThat(postViewService.getViewCountDelta(3L)).isZero();
    assertThat(postViewService.getDirtyPostIds(10)).containsExactly(2L);
  }

  @Test
  @DisplayName("트랜잭션 commit 실패 시 dirty post를 다시 등록하고 delta는 유지한다")
  void flushDirtyPostViewCountsKeepsDeltaWhenCommitFails() {
    postViewService.incrementViewCount(1L);
    when(postRepository.incrementViewCount(1L, 1L)).thenReturn(1);
    doAnswer(invocation -> executeWithTransactionSynchronization(invocation.getArgument(0), true))
        .when(transactionTemplate)
        .execute(any());

    int flushedPostCount = postViewFlushService.flushDirtyPostViewCounts(10);

    assertThat(flushedPostCount).isZero();
    assertThat(postViewService.getViewCountDelta(1L)).isEqualTo(1L);
    assertThat(postViewService.getDirtyPostIds(10)).containsExactly(1L);
  }

  @Test
  @DisplayName("flush가 delta 0을 읽은 직후 새 조회수가 들어와도 dirty post를 유지한다")
  void flushDirtyPostViewCountsKeepsDirtyPostWhenDeltaTurnsPositiveAfterZeroRead() {
    Long postId = 1L;
    postViewService =
        new TestPostViewService() {
          private boolean incrementedAfterZeroRead;

          @Override
          public long getViewCountDelta(Long requestedPostId) {
            long delta = super.getViewCountDelta(requestedPostId);
            if (requestedPostId.equals(postId) && delta == 0L && !incrementedAfterZeroRead) {
              incrementedAfterZeroRead = true;
              incrementViewCount(requestedPostId);
              return 0L;
            }
            return delta;
          }
        };
    postViewFlushService =
        new PostViewFlushService(postViewService, postRepository, transactionTemplate);
    postViewService.markDirtyPost(postId);

    int flushedPostCount = postViewFlushService.flushDirtyPostViewCounts(10);

    assertThat(flushedPostCount).isZero();
    assertThat(postViewService.getViewCountDelta(postId)).isEqualTo(1L);
    assertThat(postViewService.getDirtyPostIds(10)).containsExactly(postId);
  }

  private Object executeWithTransactionSynchronization(TransactionCallback<?> callback) {
    return executeWithTransactionSynchronization(callback, false);
  }

  private Object executeWithTransactionSynchronization(
      TransactionCallback<?> callback, boolean failCommit) {
    TransactionSynchronizationManager.initSynchronization();
    try {
      Object result = callback.doInTransaction(mock(TransactionStatus.class));
      for (TransactionSynchronization synchronization :
          TransactionSynchronizationManager.getSynchronizations()) {
        if (!failCommit) {
          synchronization.afterCommit();
          synchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
        } else {
          synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        }
      }

      if (failCommit) {
        throw new RuntimeException("commit failure");
      }
      return result;
    } finally {
      TransactionSynchronizationManager.clearSynchronization();
    }
  }
}
