package com.stocat.amumal.stock.client;

import com.stocat.amumal.stock.dto.StockSyncResponse;
import java.time.LocalDateTime;

public interface StockSyncClient {

  StockSyncResponse fetchUpdatedStocks(LocalDateTime updatedAfter, int limit);
}
