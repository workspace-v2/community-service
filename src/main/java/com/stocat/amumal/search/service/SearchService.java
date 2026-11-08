package com.stocat.amumal.search.service;

import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.menu.service.MenuService;
import com.stocat.amumal.post.dto.PostSearchSliceResponse;
import com.stocat.amumal.post.dto.PostSearchSort;
import com.stocat.amumal.post.service.PostService;
import com.stocat.amumal.post.validator.PostValidator;
import com.stocat.amumal.search.config.SearchProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * URL, 파라미터, 응답 구조, 필터, 페이지 범위를 유지하면서 설정된 검색 엔진으로 요청을 전달한다.
 */
@Service
public class SearchService {
  private final MenuService menus;
  private final PostService posts;
  private final PostValidator validator;
  private final ElasticsearchSearchClient elasticsearch;

  public SearchService(
      MenuService menus,
      PostService posts,
      PostValidator validator,
      SearchProperties properties,
      ObjectProvider<ElasticsearchSearchClient> clients) {
    this.menus = menus;
    this.posts = posts;
    this.validator = validator;
    this.elasticsearch =
        properties.engine() == SearchProperties.Engine.ELASTICSEARCH ? clients.getObject() : null;
  }

  public MenuService.Slice menus(
      String keyword, Long brandId, Integer maxPrice, int offset, int limit, boolean relevance) {
    if (elasticsearch == null)
      return menus.list(keyword, brandId, maxPrice, offset, limit, relevance);
    validator.validateListSize(limit);
    if (offset < 0 || offset > 10000) throw new ApiException(ErrorCode.INVALID_SEARCH);
    if (keyword != null) validator.validateSearch(keyword, offset, limit);
    if (brandId != null && brandId <= 0 || maxPrice != null && maxPrice < 0)
      throw new IllegalArgumentException("필터는 양수여야 합니다.");
    return elasticsearch.menus(
        keyword == null ? null : keyword.trim(), brandId, maxPrice, offset, limit, relevance);
  }

  public PostSearchSliceResponse reviews(
      Long menuId, String keyword, int offset, int limit, PostSearchSort sort) {
    if (elasticsearch == null) return posts.searchPosts(menuId, keyword, offset, limit, sort);
    validator.validateSearch(keyword, offset, limit);
    if (menuId != null && menuId <= 0) throw new IllegalArgumentException("메뉴 ID는 양수여야 합니다.");
    return elasticsearch.reviews(menuId, keyword.trim(), offset, limit, sort);
  }
}
