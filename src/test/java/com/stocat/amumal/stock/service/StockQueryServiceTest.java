package com.stocat.amumal.stock.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.stock.domain.StockSnapshot;
import com.stocat.amumal.stock.domain.StockStatus;
import com.stocat.amumal.stock.dto.StockQueryItemResponse;
import com.stocat.amumal.stock.dto.StockQueryResponse;
import com.stocat.amumal.stock.repository.StockSnapshotRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

class StockQueryServiceTest {

  private final StockSnapshotRepository stockSnapshotRepository =
      org.mockito.Mockito.mock(StockSnapshotRepository.class);

  private final StockQueryService stockQueryService =
      new StockQueryService(stockSnapshotRepository);

  @Test
  void symbol로_단건_조회한다() {
    when(stockSnapshotRepository.findById("005930"))
        .thenReturn(Optional.of(stock("005930", "삼성전자")));

    StockQueryItemResponse response = stockQueryService.getStock("005930");

    assertThat(response.symbol()).isEqualTo("005930");
    assertThat(response.name()).isEqualTo("삼성전자");
  }

  @Test
  void 없는_symbol이면_예외를_던진다() {
    when(stockSnapshotRepository.findById("999999")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> stockQueryService.getStock("999999"))
        .isInstanceOf(ApiException.class)
        .extracting(exception -> ((ApiException) exception).getErrorCode())
        .isEqualTo(ErrorCode.STOCK_NOT_FOUND);
  }

  @Test
  void symbol_정확_일치로_조회한다() {
    when(stockSnapshotRepository.findById("005930"))
        .thenReturn(Optional.of(stock("005930", "삼성전자")));

    StockQueryResponse response = stockQueryService.getStocks("005930", null, 10);

    assertThat(response.items()).hasSize(1);
    assertThat(response.items().get(0).symbol()).isEqualTo("005930");
    assertThat(response.hasNext()).isFalse();
  }

  @Test
  void name_정확_일치로_조회한다() {
    when(stockSnapshotRepository.findAllByName("삼성전자", Pageable.ofSize(11)))
        .thenReturn(List.of(stock("005930", "삼성전자")));

    StockQueryResponse response = stockQueryService.getStocks(null, "삼성전자", 10);

    assertThat(response.items()).hasSize(1);
    assertThat(response.items().get(0).name()).isEqualTo("삼성전자");
  }

  @Test
  void symbol과_name을_함께_주면_and_조건으로_조회한다() {
    when(stockSnapshotRepository.findById("005930"))
        .thenReturn(Optional.of(stock("005930", "삼성전자")));

    StockQueryResponse response = stockQueryService.getStocks("005930", "삼성전자", 10);

    assertThat(response.items()).hasSize(1);
    assertThat(response.items().get(0).symbol()).isEqualTo("005930");
  }

  @Test
  void name_조회는_limit_기준으로_hasNext를_계산한다() {
    when(stockSnapshotRepository.findAllByName("삼성전자", Pageable.ofSize(2)))
        .thenReturn(List.of(stock("005930", "삼성전자"), stock("005931", "삼성전자")));

    StockQueryResponse response = stockQueryService.getStocks(null, "삼성전자", 1);

    assertThat(response.items()).hasSize(1);
    assertThat(response.hasNext()).isTrue();
  }

  private StockSnapshot stock(String symbol, String name) {
    return StockSnapshot.of(
        symbol,
        name,
        "KOSPI",
        StockStatus.ACTIVE,
        LocalDate.of(1975, 6, 11),
        null,
        LocalDateTime.of(2026, 8, 17, 9, 0),
        LocalDateTime.of(2026, 8, 17, 9, 1));
  }
}
