package com.stocat.amumal.stock.dto;

import com.stocat.amumal.stock.domain.StockStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record StockSyncItemResponse(
    String symbol,
    String name,
    String market,
    StockStatus status,
    LocalDate listedAt,
    LocalDate delistedAt,
    LocalDateTime updatedAt) {}
