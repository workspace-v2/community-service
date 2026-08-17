package com.stocat.amumal.post.service;

import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.post.dto.PostStockResponse;
import com.stocat.amumal.stock.domain.StockSnapshot;
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

  /**
   * 게시글 작성에 사용할 종목 스냅샷을 조회한다.
   *
   * <p>커뮤니티는 상장폐지나 거래정지 종목도 토론 대상이 될 수 있으므로, 작성 가능 여부를 ACTIVE 상태로 제한하지 않는다. 존재하는 종목 코드인지까지만 확인하고 상태
   * 정보는 표시용으로만 유지한다.
   */
  public StockSnapshot getStockSnapshot(String symbol) {
    return stockSnapshotRepository
        .findById(symbol)
        .orElseThrow(() -> new ApiException(ErrorCode.INVALID_POST_SYMBOL));
  }

  /**
   * 게시글 응답에 필요한 종목 표시 정보를 여러 건 조회한다.
   *
   * <p>존재하는 종목만 맵에 담고, 중복 symbol은 첫 값을 유지한다.
   */
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

  /** 게시글 응답에 넣을 단일 종목 표시 DTO로 변환한다. */
  public PostStockResponse toPostStockResponse(StockSnapshot stockSnapshot) {
    return new PostStockResponse(
        stockSnapshot.getSymbol(), stockSnapshot.getName(), stockSnapshot.getMarket());
  }

  /** 종목 정보를 찾지 못한 경우 symbol만 보존한 빈 응답을 만든다. */
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
