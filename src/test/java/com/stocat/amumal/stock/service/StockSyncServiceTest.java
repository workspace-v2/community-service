package com.stocat.amumal.stock.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stocat.amumal.stock.client.StockSyncClient;
import com.stocat.amumal.stock.domain.StockSnapshot;
import com.stocat.amumal.stock.domain.StockStatus;
import com.stocat.amumal.stock.domain.StockSyncCheckpoint;
import com.stocat.amumal.stock.dto.StockSyncItemResponse;
import com.stocat.amumal.stock.dto.StockSyncResponse;
import com.stocat.amumal.stock.repository.StockSnapshotRepository;
import com.stocat.amumal.stock.repository.StockSyncCheckpointRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
    properties = {
      "spring.profiles.include=",
      "app.stock-sync.base-url=http://localhost:8081",
      "app.stock-sync.batch-size=2"
    })
class StockSyncServiceTest {

  @Autowired private StockSyncService stockSyncService;
  @Autowired private StockSnapshotRepository stockSnapshotRepository;
  @Autowired private StockSyncCheckpointRepository stockSyncCheckpointRepository;

  @MockitoBean private StockSyncClient stockSyncClient;

  @Test
  @DisplayName("종목 증분 동기화는 스냅샷을 upsert하고 checkpoint를 갱신한다")
  void syncStocksUpsertsSnapshotsAndUpdatesCheckpoint() {
    LocalDateTime firstUpdatedAt = LocalDateTime.of(2026, 8, 17, 9, 0);
    LocalDateTime secondUpdatedAt = LocalDateTime.of(2026, 8, 17, 9, 5);

    when(stockSyncClient.fetchUpdatedStocks(any(LocalDateTime.class), eq(2)))
        .thenReturn(
            new StockSyncResponse(
                List.of(
                    new StockSyncItemResponse(
                        "005930",
                        "삼성전자",
                        "KOSPI",
                        StockStatus.ACTIVE,
                        LocalDate.of(1975, 6, 11),
                        null,
                        firstUpdatedAt),
                    new StockSyncItemResponse(
                        "000660",
                        "SK하이닉스",
                        "KOSPI",
                        StockStatus.ACTIVE,
                        LocalDate.of(1996, 12, 26),
                        null,
                        secondUpdatedAt)),
                secondUpdatedAt,
                false));

    int syncedCount = stockSyncService.syncStocks();

    assertThat(syncedCount).isEqualTo(2);

    StockSnapshot samsung = stockSnapshotRepository.findById("005930").orElseThrow();
    assertThat(samsung.getName()).isEqualTo("삼성전자");
    assertThat(samsung.getStatus()).isEqualTo(StockStatus.ACTIVE);
    assertThat(samsung.getSourceUpdatedAt()).isEqualTo(firstUpdatedAt);

    StockSyncCheckpoint checkpoint =
        stockSyncCheckpointRepository.findById("stock_snapshot").orElseThrow();
    assertThat(checkpoint.getLastSyncedAt()).isEqualTo(secondUpdatedAt);

    verify(stockSyncClient, atLeastOnce()).fetchUpdatedStocks(any(LocalDateTime.class), eq(2));
  }
}
