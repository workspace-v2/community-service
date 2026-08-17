package com.stocat.amumal.stock.service;

import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.stock.domain.StockSnapshot;
import com.stocat.amumal.stock.dto.StockQueryItemResponse;
import com.stocat.amumal.stock.dto.StockQueryResponse;
import com.stocat.amumal.stock.repository.StockSnapshotRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockQueryService {

  private final StockSnapshotRepository stockSnapshotRepository;

  public StockQueryItemResponse getStock(String symbol) {
    String normalizedSymbol = normalize(symbol);
    StockSnapshot stockSnapshot =
        stockSnapshotRepository
            .findById(normalizedSymbol)
            .orElseThrow(() -> new ApiException(ErrorCode.STOCK_NOT_FOUND));
    return StockQueryItemResponse.from(stockSnapshot);
  }

  /**
   * 로컬에 복제된 종목 스냅샷에서 정확 조건 조회를 수행한다.
   *
   * <p>정확 조회는 게시글 작성 전 종목 선택 UI를 위한 것이므로 symbol, name 모두 부분 검색이 아니라 exact match만 허용한다. symbol과
   * name을 함께 주면 AND 조건으로 동작한다.
   */
  public StockQueryResponse getStocks(String symbol, String name, int limit) {
    List<StockSnapshot> stocks = findStocks(symbol, name, limit + 1);
    boolean hasNext = stocks.size() > limit;
    List<StockQueryItemResponse> items =
        stocks.stream().limit(limit).map(StockQueryItemResponse::from).toList();
    return new StockQueryResponse(items, hasNext);
  }

  private List<StockSnapshot> findStocks(String symbol, String name, int limit) {
    String normalizedSymbol = normalize(symbol);
    String normalizedName = normalize(name);

    if (normalizedSymbol != null && normalizedName != null) {
      return stockSnapshotRepository
          .findById(normalizedSymbol)
          .filter(stock -> stock.getName().equals(normalizedName))
          .stream()
          .toList();
    }
    if (normalizedSymbol != null) {
      return stockSnapshotRepository.findById(normalizedSymbol).stream().toList();
    }
    return stockSnapshotRepository.findAllByName(normalizedName, PageRequest.of(0, limit));
  }

  private String normalize(String value) {
    if (value == null) {
      return null;
    }

    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
