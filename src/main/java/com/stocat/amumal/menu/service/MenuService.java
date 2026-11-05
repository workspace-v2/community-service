package com.stocat.amumal.menu.service;

import static com.stocat.amumal.menu.domain.QBrand.brand;
import static com.stocat.amumal.menu.domain.QMenu.menu;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.menu.domain.Brand;
import com.stocat.amumal.menu.domain.Menu;
import com.stocat.amumal.menu.dto.MenuResponse;
import com.stocat.amumal.menu.repository.MenuRepository;
import com.stocat.amumal.post.validator.PostValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {
  private final JPAQueryFactory queryFactory;
  private final MenuRepository menuRepository;
  private final PostValidator validator;

  public record Slice(List<MenuResponse> content, int offset, int limit, boolean hasNext) {}

  public record BrandResponse(Long id, String name) {}

  public record BrandSlice(List<BrandResponse> content, int offset, int limit, boolean hasNext) {}

  public BrandSlice brands(int offset, int limit) {
    validatePage(offset, limit);
    List<Brand> rows =
        queryFactory
            .selectFrom(brand)
            .where(brand.deletedAt.isNull())
            .orderBy(brand.id.asc())
            .offset(offset)
            .limit(limit + 1L)
            .fetch();
    return new BrandSlice(
        rows.stream().limit(limit).map(b -> new BrandResponse(b.getId(), b.getName())).toList(),
        offset,
        limit,
        rows.size() > limit);
  }

  public MenuResponse get(Long menuId) {
    Menu m =
        menuRepository
            .findById(menuId)
            .orElseThrow(() -> new ApiException(ErrorCode.MENU_NOT_FOUND));
    if (m.isDeleted() || m.getBrand().isDeleted()) throw new ApiException(ErrorCode.MENU_NOT_FOUND);
    return MenuResponse.from(m);
  }

  private void validatePage(int offset, int limit) {
    validator.validateListSize(limit);
    if (offset < 0 || offset > 10000) throw new ApiException(ErrorCode.INVALID_SEARCH);
  }

  public Slice list(
      String keyword, Long brandId, Integer maxPrice, int offset, int limit, boolean relevance) {
    validatePage(offset, limit);
    if (keyword != null) {
      validator.validateSearch(keyword, offset, limit);
      keyword = keyword.trim();
    }
    if (maxPrice != null && maxPrice < 0 || brandId != null && brandId <= 0)
      throw new IllegalArgumentException("필터는 양수여야 합니다.");
    BooleanExpression matches =
        keyword == null
            ? null
            : menu.name
                .containsIgnoreCase(keyword)
                .or(menu.description.containsIgnoreCase(keyword))
                .or(menu.brand.name.containsIgnoreCase(keyword));
    JPAQuery<Menu> q =
        queryFactory
            .selectFrom(menu)
            .join(menu.brand)
            .where(
                menu.deletedAt.isNull(),
                menu.brand.deletedAt.isNull(),
                matches,
                brandId == null ? null : menu.brand.id.eq(brandId),
                maxPrice == null ? null : menu.price.loe(maxPrice))
            .offset(offset)
            .limit(limit + 1L);
    if (relevance && keyword != null)
      q.orderBy(
          new CaseBuilder().when(menu.name.containsIgnoreCase(keyword)).then(1).otherwise(0).desc(),
          new CaseBuilder()
              .when(menu.description.containsIgnoreCase(keyword))
              .then(1)
              .otherwise(0)
              .desc());
    List<Menu> rows = q.orderBy(menu.id.desc()).fetch();
    return new Slice(
        rows.stream().limit(limit).map(MenuResponse::from).toList(),
        offset,
        limit,
        rows.size() > limit);
  }
}
