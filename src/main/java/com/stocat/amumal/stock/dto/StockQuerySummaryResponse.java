package com.stocat.amumal.stock.dto;

import com.stocat.amumal.stock.domain.StockSnapshot;
import com.stocat.amumal.stock.domain.StockStatus;

/** 게시글 작성 화면에 필요한 최소 종목 정보 */
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
