package com.stocat.amumal.stock.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stock_snapshot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockSnapshot {

  @Id
  @Column(length = 6, nullable = false)
  private String symbol;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, length = 20)
  private String market;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private StockStatus status;

  private LocalDate listedAt;

  private LocalDate delistedAt;

  @Column(nullable = false)
  private LocalDateTime sourceUpdatedAt;

  @Column(nullable = false)
  private LocalDateTime syncedAt;

  public static StockSnapshot of(
      String symbol,
      String name,
      String market,
      StockStatus status,
      LocalDate listedAt,
      LocalDate delistedAt,
      LocalDateTime sourceUpdatedAt,
      LocalDateTime syncedAt) {
    StockSnapshot stockSnapshot = new StockSnapshot();
    stockSnapshot.symbol = symbol;
    stockSnapshot.name = name;
    stockSnapshot.market = market;
    stockSnapshot.status = status;
    stockSnapshot.listedAt = listedAt;
    stockSnapshot.delistedAt = delistedAt;
    stockSnapshot.sourceUpdatedAt = sourceUpdatedAt;
    stockSnapshot.syncedAt = syncedAt;
    return stockSnapshot;
  }
}
