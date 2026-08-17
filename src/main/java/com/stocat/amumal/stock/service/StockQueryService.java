package com.stocat.amumal.stock.service;

import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.stock.domain.StockSnapshot;
import com.stocat.amumal.stock.dto.StockQueryResponse;
import com.stocat.amumal.stock.dto.StockQuerySummaryResponse;
import com.stocat.amumal.stock.repository.StockSnapshotRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/** 게시글 작성용 종목 검색을 로컬 stock_snapshot 읽기 모델에서 처리한다. */
public class StockQueryService {

  private final StockSnapshotRepository stockSnapshotRepository;

  /** 선택된 종목 symbol 하나를 다시 읽어와 표시할 때 사용한다. */
  public StockQuerySummaryResponse findStockBySymbol(String symbol) {
    String normalizedSymbol = normalize(symbol);
    StockSnapshot stockSnapshot =
        stockSnapshotRepository
            .findById(normalizedSymbol)
            .orElseThrow(() -> new ApiException(ErrorCode.STOCK_NOT_FOUND));
    return StockQuerySummaryResponse.from(stockSnapshot);
  }

  /**
   * 로컬에 복제된 종목 스냅샷에서 글 작성용 종목 검색을 수행한다.
   *
   * <p>symbol은 정확 일치, name은 포함 검색 기준으로 조회한다. symbol과 name을 함께 주면 symbol exact + name contains의 AND
   * 조건으로 동작한다.
   */
  public StockQueryResponse searchStocks(String symbol, String name, int limit) {
    List<StockSnapshot> stocks = findStocks(symbol, name, limit + 1);
    boolean hasNext = stocks.size() > limit;
    List<StockQuerySummaryResponse> items =
        stocks.stream().limit(limit).map(StockQuerySummaryResponse::from).toList();
    return new StockQueryResponse(items, hasNext);
  }

  private List<StockSnapshot> findStocks(String symbol, String name, int limit) {
    String normalizedSymbol = normalize(symbol);
    String normalizedName = normalize(name);

    if (normalizedSymbol != null && normalizedName != null) {
      return stockSnapshotRepository
          .findById(normalizedSymbol)
          .filter(stock -> containsIgnoreCase(stock.getName(), normalizedName))
          .stream()
          .toList();
    }
    if (normalizedSymbol != null) {
      return stockSnapshotRepository.findById(normalizedSymbol).stream().toList();
    }
    return stockSnapshotRepository.findAllByNameContainingIgnoreCase(
        normalizedName, PageRequest.of(0, limit));
  }

  private String normalize(String value) {
    if (value == null) {
      return null;
    }

    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private boolean containsIgnoreCase(String actual, String keyword) {
    return actual != null && actual.toLowerCase().contains(keyword.toLowerCase());
  }
}
