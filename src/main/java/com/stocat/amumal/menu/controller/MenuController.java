package com.stocat.amumal.menu.controller;

import com.stocat.amumal.common.response.ApiResponse;
import com.stocat.amumal.menu.dto.MenuResponse;
import com.stocat.amumal.menu.service.MenuService;
import com.stocat.amumal.post.dto.PostSearchSort;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class MenuController {
  private final MenuService menus;

  @GetMapping("/brands")
  public ApiResponse<MenuService.BrandSlice> brands(
      @RequestParam(defaultValue = "0") @Min(0) @Max(10000) int offset,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {
    return ApiResponse.of("브랜드 목록 조회에 성공했습니다.", menus.brands(offset, limit));
  }

  @GetMapping("/menus")
  public ApiResponse<MenuService.Slice> list(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) @Positive Long brandId,
      @RequestParam(required = false) @Min(0) Integer maxPrice,
      @RequestParam(defaultValue = "0") @Min(0) @Max(10000) int offset,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit,
      @RequestParam(defaultValue = "recent") String sort) {
    return ApiResponse.of(
        "메뉴 목록 조회에 성공했습니다.",
        menus.list(
            keyword,
            brandId,
            maxPrice,
            offset,
            limit,
            PostSearchSort.from(sort) == PostSearchSort.RELEVANCE));
  }

  @GetMapping("/menus/{menuId}")
  public ApiResponse<MenuResponse> get(@PathVariable @Positive Long menuId) {
    return ApiResponse.of("메뉴 조회에 성공했습니다.", menus.get(menuId));
  }
}
