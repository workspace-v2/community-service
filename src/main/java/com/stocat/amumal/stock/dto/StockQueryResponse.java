package com.stocat.amumal.stock.dto;

import java.util.List;

public record StockQueryResponse(List<StockQueryItemResponse> items, boolean hasNext) {}
