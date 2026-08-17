package com.stocat.amumal.stock.dto;

import com.stocat.amumal.stock.domain.StockSnapshot;
import com.stocat.amumal.stock.domain.StockStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record StockQueryItemResponse(
    String symbol,
    String name,
    String market,
    StockStatus status,
    LocalDate listedAt,
    LocalDate delistedAt,
    LocalDateTime updatedAt) {

  public static StockQueryItemResponse from(StockSnapshot stockSnapshot) {
    return new StockQueryItemResponse(
        stockSnapshot.getSymbol(),
        stockSnapshot.getName(),
        stockSnapshot.getMarket(),
        stockSnapshot.getStatus(),
        stockSnapshot.getListedAt(),
        stockSnapshot.getDelistedAt(),
        stockSnapshot.getSourceUpdatedAt());
  }
}
