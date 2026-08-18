package com.stocat.amumal.stock.client;

import com.stocat.amumal.stock.config.StockSyncProperties;
import com.stocat.amumal.stock.dto.StockSyncResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** market-data-service의 종목 동기화 API를 호출해 변경분을 읽어오는 HTTP 클라이언트다. */
@Component
@RequiredArgsConstructor
public class MarketDataStockSyncClient implements StockSyncClient {

  private final RestClient.Builder restClientBuilder;
  private final StockSyncProperties stockSyncProperties;

  /** 마지막 동기화 시각 이후의 종목 변경분을 원격 서비스에서 조회한다. */
  @Override
  public StockSyncResponse fetchUpdatedStocks(LocalDateTime updatedAfter, int limit) {
    RestClient restClient =
        restClientBuilder.baseUrl(stockSyncProperties.baseUrl().toString()).build();

    return restClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("/internal/v1/stocks/sync")
                    .queryParam("updatedAfter", updatedAfter)
                    .queryParam("limit", limit)
                    .build())
        .retrieve()
        .body(StockSyncResponse.class);
  }
}
