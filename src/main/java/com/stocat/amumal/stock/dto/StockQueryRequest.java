package com.stocat.amumal.stock.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StockQueryRequest {

  @Pattern(regexp = "\\d{6}")
  private String symbol;

  private String name;

  @Positive
  @Max(100)
  private int limit = 20;

  @AssertTrue
  public boolean hasCondition() {
    return !isBlank(symbol) || !isBlank(name);
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
