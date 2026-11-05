package com.stocat.amumal.search.controller;

import com.stocat.amumal.common.response.ApiResponse;
import com.stocat.amumal.menu.service.MenuService;
import com.stocat.amumal.post.dto.*;
import com.stocat.amumal.post.service.PostService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class SearchController {
  private final MenuService menus;
  private final PostService posts;

  public record Result(MenuService.Slice menus, PostSearchSliceResponse reviews) {}

  // 두 유형은 점수 체계가 다르므로 각각 페이지네이션한다. 전역 relevance 순위가 아니다.
  @GetMapping("/search")
  public ApiResponse<Result> search(
      @RequestParam @NotBlank @Size(max = 100) String keyword,
      @RequestParam(defaultValue = "0") @Min(0) @Max(10000) int menuOffset,
      @RequestParam(defaultValue = "0") @Min(0) @Max(10000) int reviewOffset,
      @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit,
      @RequestParam(defaultValue = "relevance") String sort) {
    PostSearchSort order = PostSearchSort.from(sort);
    return ApiResponse.of(
        "메뉴와 버거 리뷰 검색에 성공했습니다.",
        new Result(
            menus.list(keyword, null, null, menuOffset, limit, order == PostSearchSort.RELEVANCE),
            posts.searchPosts(null, keyword, reviewOffset, limit, order)));
  }
}
