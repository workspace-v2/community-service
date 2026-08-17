package com.stocat.amumal.stock.dto;

import com.stocat.amumal.stock.domain.StockSnapshot;
import com.stocat.amumal.stock.domain.StockStatus;

public record StockQuerySummaryResponse(
    String symbol, String name, String market, StockStatus status) {

  public static StockQuerySummaryResponse from(StockSnapshot stockSnapshot) {
    return new StockQuerySummaryResponse(
        stockSnapshot.getSymbol(),
        stockSnapshot.getName(),
        stockSnapshot.getMarket(),
        stockSnapshot.getStatus());
  }
}
