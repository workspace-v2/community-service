package com.stocat.amumal.stock.dto;

import java.util.List;

public record StockQueryResponse(List<StockQuerySummaryResponse> items, boolean hasNext) {}
