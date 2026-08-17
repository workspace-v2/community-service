package com.stocat.amumal.stock.controller;

import com.stocat.amumal.common.response.ApiResponse;
import com.stocat.amumal.stock.dto.StockQueryRequest;
import com.stocat.amumal.stock.dto.StockQueryResponse;
import com.stocat.amumal.stock.dto.StockQuerySummaryResponse;
import com.stocat.amumal.stock.service.StockQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/stocks")
/** 게시글 작성 화면에서 종목을 찾고 선택하기 위한 검색 API */
public class StockController {

  private final StockQueryService stockQueryService;

  @GetMapping("/{symbol}")
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse<StockQuerySummaryResponse> getStock(
      @PathVariable("symbol") @Pattern(regexp = "\\d{6}") String symbol) {
    return ApiResponse.of("종목 조회에 성공했습니다.", stockQueryService.getStock(symbol));
  }

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public ApiResponse<StockQueryResponse> getStocks(
      @Valid @ModelAttribute StockQueryRequest request) {
    return ApiResponse.of(
        "종목 조회에 성공했습니다.",
        stockQueryService.getStocks(request.getSymbol(), request.getName(), request.getLimit()));
  }
}
