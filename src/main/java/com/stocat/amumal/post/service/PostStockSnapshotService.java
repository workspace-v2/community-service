package com.stocat.amumal.post.service;

import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.post.dto.PostStockResponse;
import com.stocat.amumal.stock.domain.StockSnapshot;
import com.stocat.amumal.stock.domain.StockStatus;
import com.stocat.amumal.stock.repository.StockSnapshotRepository;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostStockSnapshotService {

  private final StockSnapshotRepository stockSnapshotRepository;

  public StockSnapshot getActiveStockSnapshot(String symbol) {
    StockSnapshot stockSnapshot =
        stockSnapshotRepository
            .findById(symbol)
            .orElseThrow(() -> new ApiException(ErrorCode.INVALID_POST_SYMBOL));

    if (stockSnapshot.getStatus() != StockStatus.ACTIVE) {
      throw new ApiException(ErrorCode.UNAVAILABLE_POST_SYMBOL);
    }

    return stockSnapshot;
  }

  public Map<String, PostStockResponse> getStockResponses(Collection<String> symbols) {
    if (symbols == null || symbols.isEmpty()) {
      return Collections.emptyMap();
    }

    return stockSnapshotRepository.findAllBySymbolIn(symbols).stream()
        .collect(
            Collectors.toMap(
                StockSnapshot::getSymbol,
                stock ->
                    new PostStockResponse(stock.getSymbol(), stock.getName(), stock.getMarket()),
                (left, right) -> left));
  }

  public PostStockResponse toPostStockResponse(StockSnapshot stockSnapshot) {
    return new PostStockResponse(
        stockSnapshot.getSymbol(), stockSnapshot.getName(), stockSnapshot.getMarket());
  }

  public PostStockResponse emptyStockResponse(String symbol) {
    return new PostStockResponse(symbol, null, null);
  }

  public boolean existsSymbol(String symbol) {
    return symbol != null && stockSnapshotRepository.existsById(symbol.trim());
  }

  public Map<String, PostStockResponse> getExistingStockResponses(Collection<String> symbols) {
    return getStockResponses(symbols.stream().filter(Objects::nonNull).collect(Collectors.toSet()));
  }
}
