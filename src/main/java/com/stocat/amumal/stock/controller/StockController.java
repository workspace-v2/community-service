package com.stocat.amumal.stock.controller;

import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.common.response.ApiResponse;
import com.stocat.amumal.stock.dto.StockQueryItemResponse;
import com.stocat.amumal.stock.dto.StockQueryResponse;
import com.stocat.amumal.stock.service.StockQueryService;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/stocks")
public class StockController {

  private static final int MAX_LIMIT = 100;

  private final StockQueryService stockQueryService;

  @GetMapping("/{symbol}")
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse<StockQueryItemResponse> getStock(
      @PathVariable("symbol") @Pattern(regexp = "\\d{6}") String symbol) {
    return ApiResponse.of("종목 단건 조회에 성공했습니다.", stockQueryService.getStock(symbol));
  }

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse<StockQueryResponse> getStocks(
      @Pattern(regexp = "\\d{6}") @RequestParam(value = "symbol", required = false) String symbol,
      @RequestParam(value = "name", required = false) String name,
      @Positive @RequestParam(value = "limit", defaultValue = "20") int limit) {
    validateRequest(symbol, name, limit);
    return ApiResponse.of("종목 조회에 성공했습니다.", stockQueryService.getStocks(symbol, name, limit));
  }

  /**
   * 전체 목록 API로 오해되지 않게 최소 하나의 정확 조건을 강제한다.
   *
   * <p>limit 상한도 고정해 두어 종목 스냅샷 수가 커져도 커뮤니티 API가 과도한 응답을 만들지 않게 막는다.
   */
  private void validateRequest(String symbol, String name, int limit) {
    if (isBlank(symbol) && isBlank(name)) {
      throw new ApiException(ErrorCode.MISSING_STOCK_QUERY_CONDITION);
    }
    if (limit > MAX_LIMIT) {
      throw new ApiException(ErrorCode.INVALID_STOCK_QUERY_LIMIT);
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
