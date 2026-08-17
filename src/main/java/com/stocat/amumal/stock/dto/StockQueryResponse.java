package com.stocat.amumal.stock.dto;

import java.util.List;

/** 게시글 작성용 종목 검색 결과 */
public record StockQueryResponse(List<StockQuerySummaryResponse> items, boolean hasNext) {}
