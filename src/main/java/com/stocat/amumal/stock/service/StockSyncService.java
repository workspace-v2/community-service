package com.stocat.amumal.stock.service;

import com.stocat.amumal.stock.client.StockSyncClient;
import com.stocat.amumal.stock.config.StockSyncProperties;
import com.stocat.amumal.stock.domain.StockSnapshot;
import com.stocat.amumal.stock.domain.StockSyncCheckpoint;
import com.stocat.amumal.stock.dto.StockSyncItemResponse;
import com.stocat.amumal.stock.dto.StockSyncResponse;
import com.stocat.amumal.stock.repository.StockSnapshotRepository;
import com.stocat.amumal.stock.repository.StockSyncCheckpointRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * market-data-service가 관리하는 종목 마스터를 {@code stock_snapshot} 읽기 모델로 복제한다.
 *
 * <p>{@code community}는 종목 원본을 직접 소유하지 않는다. 마지막 동기화 시각을 기준으로 증분 조회한 뒤 로컬 스냅샷을 upsert하는 역할만 수행
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockSyncService {

  private static final String STOCK_SYNC_TARGET = "stock_snapshot";
  private static final LocalDateTime INITIAL_SYNC_TIME = LocalDateTime.of(1970, 1, 1, 0, 0);

  private final StockSyncClient stockSyncClient;
  private final StockSyncProperties stockSyncProperties;
  private final StockSnapshotRepository stockSnapshotRepository;
  private final StockSyncCheckpointRepository stockSyncCheckpointRepository;

  /**
   * 마지막 성공 커서 이후의 종목 변경분을 모두 반영
   *
   * @return 이번 실행에서 upsert된 종목 수
   */
  @Transactional
  public int syncStocks() {
    StockSyncCheckpoint checkpoint = getOrCreateCheckpoint();
    LocalDateTime cursor = checkpoint.getLastSyncedAt();
    int syncedCount = 0;

    while (true) {
      StockSyncResponse response =
          stockSyncClient.fetchUpdatedStocks(cursor, stockSyncProperties.batchSize());
      List<StockSyncItemResponse> items = response.items() == null ? List.of() : response.items();

      syncedCount += upsertStocks(items);

      if (!items.isEmpty()) {
        cursor = resolveNextCursor(response, items, cursor);
        checkpoint.updateLastSyncedAt(cursor);
      }

      if (!response.hasNext()) {
        return syncedCount;
      }
    }
  }

  private StockSyncCheckpoint getOrCreateCheckpoint() {
    return stockSyncCheckpointRepository
        .findById(STOCK_SYNC_TARGET)
        .orElseGet(
            () ->
                stockSyncCheckpointRepository.save(
                    StockSyncCheckpoint.of(STOCK_SYNC_TARGET, INITIAL_SYNC_TIME)));
  }

  private int upsertStocks(List<StockSyncItemResponse> items) {
    LocalDateTime syncedAt = LocalDateTime.now();

    for (StockSyncItemResponse item : items) {
      stockSnapshotRepository
          .findById(item.symbol())
          .ifPresentOrElse(
              existing ->
                  existing.updateFrom(
                      item.name(),
                      item.market(),
                      item.status(),
                      item.listedAt(),
                      item.delistedAt(),
                      item.updatedAt(),
                      syncedAt),
              () ->
                  stockSnapshotRepository.save(
                      StockSnapshot.of(
                          item.symbol(),
                          item.name(),
                          item.market(),
                          item.status(),
                          item.listedAt(),
                          item.delistedAt(),
                          item.updatedAt(),
                          syncedAt)));
    }

    if (!items.isEmpty()) {
      log.debug("Synced {} stock snapshots", items.size());
    }
    return items.size();
  }

  private LocalDateTime resolveNextCursor(
      StockSyncResponse response, List<StockSyncItemResponse> items, LocalDateTime fallbackCursor) {
    if (response.nextUpdatedAfter() != null) {
      return response.nextUpdatedAfter();
    }

    return items.stream()
        .map(StockSyncItemResponse::updatedAt)
        .max(LocalDateTime::compareTo)
        .orElse(fallbackCursor);
  }
}
