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

/**
 * community가 로컬에서 참조하는 종목 읽기 모델이다.
 *
 * <p>원본 종목 마스터는 외부 서비스에 있고, 이 엔티티는 게시글 작성 검증과 종목명 표시를 위한 복제본만 유지한다.
 */
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

  /**
   * 원본 종목 마스터에서 받은 최신 상태로 읽기 모델을 덮어쓴다.
   *
   * <p>{@code sourceUpdatedAt}은 원본 수정 시각이고, {@code syncedAt}은 community에 반영한 시각이다.
   */
  public void updateFrom(
      String name,
      String market,
      StockStatus status,
      LocalDate listedAt,
      LocalDate delistedAt,
      LocalDateTime sourceUpdatedAt,
      LocalDateTime syncedAt) {
    this.name = name;
    this.market = market;
    this.status = status;
    this.listedAt = listedAt;
    this.delistedAt = delistedAt;
    this.sourceUpdatedAt = sourceUpdatedAt;
    this.syncedAt = syncedAt;
  }
}
