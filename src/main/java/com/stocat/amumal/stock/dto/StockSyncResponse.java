package com.stocat.amumal.stock.dto;

import java.time.LocalDateTime;
import java.util.List;

public record StockSyncResponse(
    List<StockSyncItemResponse> items, LocalDateTime nextUpdatedAfter, boolean hasNext) {}
