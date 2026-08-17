package com.stocat.amumal.stock.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stock_sync_checkpoint")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockSyncCheckpoint {

  @Id
  @Column(length = 50, nullable = false)
  private String syncTarget;

  @Column(nullable = false)
  private LocalDateTime lastSyncedAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  public static StockSyncCheckpoint of(String syncTarget, LocalDateTime lastSyncedAt) {
    StockSyncCheckpoint checkpoint = new StockSyncCheckpoint();
    checkpoint.syncTarget = syncTarget;
    checkpoint.lastSyncedAt = lastSyncedAt;
    checkpoint.updatedAt = LocalDateTime.now();
    return checkpoint;
  }

  public void updateLastSyncedAt(LocalDateTime lastSyncedAt) {
    this.lastSyncedAt = lastSyncedAt;
    this.updatedAt = LocalDateTime.now();
  }
}
