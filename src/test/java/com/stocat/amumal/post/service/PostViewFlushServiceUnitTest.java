package com.stocat.amumal.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.stocat.amumal.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
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
    postViewFlushService = new PostViewFlushService(postViewService, postRepository, transactionTemplate);

    when(transactionTemplate.execute(any()))
        .thenAnswer(
            invocation ->
                ((TransactionCallback<?>)
                        invocation.getArgument(0, TransactionCallback.class))
                    .doInTransaction(mock()));
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
}
