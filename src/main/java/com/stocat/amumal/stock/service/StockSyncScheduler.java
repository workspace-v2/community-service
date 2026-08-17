package com.stocat.amumal.stock.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 종목 읽기 모델을 주기적으로 최신 상태로 맞추는 스케줄러 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class StockSyncScheduler {

  private final StockSyncService stockSyncService;

  /** 종목 마스터는 변경 빈도가 낮으므로 분 단위 폴링으로 구현 */
  @Scheduled(fixedDelayString = "${app.stock-sync.fixed-delay-ms:300000}")
  public void syncStocks() {
    int syncedCount = stockSyncService.syncStocks();
    if (syncedCount > 0) {
      log.debug("Synced {} stocks into stock_snapshot", syncedCount);
    }
  }
}
