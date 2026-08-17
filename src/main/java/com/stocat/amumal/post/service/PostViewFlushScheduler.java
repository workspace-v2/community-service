package com.stocat.amumal.post.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@Profile("!test")
public class PostViewFlushScheduler {

  private static final int DEFAULT_FLUSH_BATCH_SIZE = 100;

  private final PostViewFlushService postViewFlushService;

  // 조회수는 약한 실시간성만 보장해도 되므로 기본 flush 주기를 30초로 설정
  // 너무 자주 flush하면 DB write 부하가 커지므로, 운영 환경에서 조정 가능하도록 프로퍼티화
  @Scheduled(fixedDelayString = "${post.view.flush.fixed-delay-ms:30000}")
  public void flushDirtyPostViewCounts() {
    int flushedPostCount = postViewFlushService.flushDirtyPostViewCounts(DEFAULT_FLUSH_BATCH_SIZE);
    if (flushedPostCount > 0) {
      log.debug("Flushed view-count deltas for {} posts", flushedPostCount);
    }
  }
}
