package com.stocat.amumal.post.service;

import static com.stocat.amumal.post.domain.QPost.post;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stocat.amumal.menu.domain.QBrand;
import com.stocat.amumal.menu.domain.QMenu;
import com.stocat.amumal.post.domain.Post;
import com.stocat.amumal.post.domain.ReviewTag;
import com.stocat.amumal.post.dto.PostSearchSort;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 버거 리뷰 목록과 검색 결과를 조회한다.
 *
 * <p>작성자·메뉴·브랜드는 함께 로딩한다.
 * 관련도 첫 페이지 중 카탈로그와 태그가 일치하지 않는 검색은 제목 우선순위를 이용해 제한된 건수의 목록 조회로 나눈다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQuerydslService {
  private final JPAQueryFactory queryFactory;

  /**
   * 삭제되지 않은 리뷰에 작성자·메뉴·브랜드 내부 fetch join을 적용한다.
   * 연결된 엔티티가 없는 리뷰는 제외하며, 이 메서드 자체는 쿼리를 실행하지 않는다.
   *
   * @param menuId 제한할 메뉴 ID, null이면 메뉴 조건을 생략한다.
   * @return 목록 조회에서 재사용할 쿼리
   */
  private JPAQuery<Post> reviewQuery(Long menuId) {
    JPAQuery<Post> query =
        queryFactory
            .selectFrom(post)
            .join(post.user)
            .fetchJoin()
            .join(post.menu)
            .fetchJoin()
            .join(post.menu.brand)
            .fetchJoin()
            .where(post.deletedAt.isNull());
    if (menuId != null) {
      query.where(post.menu.id.eq(menuId));
    }
    return query;
  }

  /**
   * ID 내림차순으로 오프셋 목록을 조회한다.
   *
   * @param offset 건너뛸 결과 수
   * @param limit 최대 조회 건수
   * @return 조회된 리뷰 목록
   */
  public List<Post> findAllByOffset(long offset, long limit) {
    return reviewQuery(null).orderBy(post.id.desc()).offset(offset).limit(limit).fetch();
  }

  /**
   * 커서보다 작은 ID의 리뷰를 ID 내림차순으로 조회한다.
   *
   * @param menuId 제한할 메뉴 ID, null이면 모든 메뉴를 대상으로 한다.
   * @param cursor 이전 페이지의 마지막 ID, null이면 첫 페이지다.
   * @param limit 최대 조회 건수
   * @return 조회된 리뷰 목록
   */
  public List<Post> findAllByCursor(Long menuId, Long cursor, long limit) {
    JPAQuery<Post> query = reviewQuery(menuId);
    if (cursor != null) {
      query.where(post.id.lt(cursor));
    }
    return query.orderBy(post.id.desc()).limit(limit).fetch();
  }

  /**
   * 제목·본문·메뉴명·브랜드명·대응 태그 중 하나라도 일치하는 리뷰를 조회한다.
   *
   * <p>관련도순은 제목 일치, 본문 일치, ID 내림차순으로 정렬한다.
   * 메뉴·브랜드 및 대응 태그가 일치하지 않는 관련도 첫 페이지는 제목 목록을 먼저 조회하고 본문 단독 목록으로 채운다.
   *
   * <p>호출부에서 검색어·페이지 범위를 검증하고 검색어의 앞뒤 공백을 제거해야 한다. 이 조회 계층은 검증을 반복하지 않는다.
   * sort는 null이 아닌 정렬 값을 전달한다.
   *
   * @param menuId 제한할 메뉴 ID, null이면 메뉴 조건을 생략한다.
   * @param keyword 공백 제거된 비어 있지 않은 검색어, 최대 100자
   * @param offset 건너뛸 결과 수, 호출 경로의 허용 범위는 0~10000
   * @param limit 양수인 최대 조회 건수, 호출부는 페이지 크기 1~100에 다음 페이지 확인용 1건을 더한다.
   * @param sort 관련도순 또는 최신순
   * @return 정렬된 리뷰 목록, 일치하는 결과가 없으면 빈 목록
   */
  public List<Post> searchPosts(
      Long menuId, String keyword, long offset, long limit, PostSearchSort sort) {
    List<ReviewTag> tags = ReviewTag.matching(keyword);
    BooleanExpression titleMatches = post.title.containsIgnoreCase(keyword);
    BooleanExpression bodyMatches = post.content.containsIgnoreCase(keyword);
    boolean useCatalogSubquery = usesCatalogSubquery(sort, menuId, tags);
    boolean firstPage = offset == 0;

    if (useCatalogSubquery && firstPage && limit > 0) {
      boolean catalogMatches = hasMatchingCatalog(keyword);
      if (!catalogMatches) {
        return findFirstTextPage(titleMatches, bodyMatches, limit);
      }
    }

    BooleanExpression catalogMatches;
    if (useCatalogSubquery) {
      catalogMatches = matchingMenuIds(keyword);
    } else {
      catalogMatches =
          post.menu
              .name
              .containsIgnoreCase(keyword)
              .or(post.menu.brand.name.containsIgnoreCase(keyword));
    }
    BooleanExpression matches =
        combineSearchConditions(titleMatches, bodyMatches, catalogMatches, tags);
    return reviewQuery(menuId)
        .where(matches)
        .orderBy(searchOrder(sort, titleMatches, bodyMatches))
        .offset(offset)
        .limit(limit)
        .fetch();
  }

  /** 메뉴 ID 서브쿼리를 사용하는 조건인 관련도순·메뉴 필터 없음·대응 태그 없음을 판별한다. */
  private boolean usesCatalogSubquery(PostSearchSort sort, Long menuId, List<ReviewTag> tags) {
    boolean relevanceOrder = sort == PostSearchSort.RELEVANCE;
    boolean noMenuFilter = menuId == null;
    boolean noMatchingTags = tags.isEmpty();
    return relevanceOrder && noMenuFilter && noMatchingTags;
  }

  /** 제목·본문·카탈로그와 선택적인 태그 조건을 OR로 연결한다. 쿼리는 실행하지 않는다. */
  private BooleanExpression combineSearchConditions(
      BooleanExpression titleMatches,
      BooleanExpression bodyMatches,
      BooleanExpression catalogMatches,
      List<ReviewTag> tags) {
    BooleanExpression matches = titleMatches.or(bodyMatches).or(catalogMatches);
    if (!tags.isEmpty()) {
      matches = matches.or(post.tags.any().in(tags));
    }
    return matches;
  }

  /** 쿼리를 변경하지 않고 관련도순 또는 최신순의 정렬 기준을 반환한다. */
  private OrderSpecifier<?>[] searchOrder(
      PostSearchSort sort, BooleanExpression titleMatches, BooleanExpression bodyMatches) {
    if (sort == PostSearchSort.RELEVANCE) {
      return new OrderSpecifier<?>[] {
        matchPriority(titleMatches).desc(), matchPriority(bodyMatches).desc(), post.id.desc()
      };
    }
    return new OrderSpecifier<?>[] {post.id.desc()};
  }

  /** 메뉴명 또는 해당 메뉴의 브랜드명에 검색어가 포함되는 서브쿼리용 조건을 만든다. */
  private BooleanExpression catalogNameMatches(QMenu menu, String keyword) {
    QBrand brand = new QBrand("matchingBrand");
    return menu.name
        .containsIgnoreCase(keyword)
        .or(
            menu.brand.id.in(
                JPAExpressions.select(brand.id)
                    .from(brand)
                    .where(brand.name.containsIgnoreCase(keyword))));
  }

  /** 메뉴명 또는 브랜드명이 일치하는 메뉴가 하나라도 있는지 DB에서 확인한다. 리뷰 존재 여부는 검사하지 않는다. */
  private boolean hasMatchingCatalog(String keyword) {
    QMenu menu = new QMenu("matchingMenu");
    return queryFactory.selectOne().from(menu).where(catalogNameMatches(menu, keyword)).fetchFirst()
        != null;
  }

  /** 리뷰의 메뉴 ID가 검색어와 일치하는 메뉴 ID 서브쿼리에 포함되는 조건을 만든다. */
  private BooleanExpression matchingMenuIds(String keyword) {
    QMenu menu = new QMenu("matchingMenu");
    return post.menu.id.in(
        JPAExpressions.select(menu.id).from(menu).where(catalogNameMatches(menu, keyword)));
  }

  /**
   * 카탈로그/태그 일치가 없으므로 제목 일치가 본문 단독 일치보다 항상 앞선다.
   * 먼저 존재 여부를 확인해 미검색은 전체 스캔을 반복하지 않고 끝낸다.
   * 같은 읽기 트랜잭션에서 최대 limit건만 모으며, 본문 단계는 제목 일치를 제외해 중복을 막는다.
   */
  private List<Post> findFirstTextPage(
      BooleanExpression titleMatches, BooleanExpression bodyMatches, long limit) {
    if (!hasMatchingText(titleMatches, bodyMatches)) {
      return List.of();
    }
    List<Post> result = new ArrayList<>(findTitleMatches(titleMatches, bodyMatches, limit));
    long remaining = limit - result.size();
    if (remaining > 0) {
      result.addAll(findBodyOnlyMatches(titleMatches, bodyMatches, remaining));
    }
    return result;
  }

  /** 목록과 같은 내부 조인·삭제 조건에서 제목 또는 본문 일치 리뷰의 존재 여부만 확인한다. */
  private boolean hasMatchingText(BooleanExpression titleMatches, BooleanExpression bodyMatches) {
    return queryFactory
            .selectOne()
            .from(post)
            .join(post.user)
            .join(post.menu)
            .join(post.menu.brand)
            .where(post.deletedAt.isNull(), titleMatches.or(bodyMatches))
            .fetchFirst()
        != null;
  }

  /** 제목 일치 리뷰 중 본문도 일치하는 리뷰를 먼저 조회하고, 같은 우선순위는 ID 내림차순으로 정렬한다. */
  private List<Post> findTitleMatches(
      BooleanExpression titleMatches, BooleanExpression bodyMatches, long limit) {
    return reviewQuery(null)
        .where(titleMatches)
        .orderBy(matchPriority(bodyMatches).desc(), post.id.desc())
        .limit(limit)
        .fetch();
  }

  /** 제목 일치 리뷰를 제외한 본문 일치 리뷰를 조회한다. NULL 제목도 포함해 앞 단계와 중복되지 않게 한다. */
  private List<Post> findBodyOnlyMatches(
      BooleanExpression titleMatches, BooleanExpression bodyMatches, long limit) {
    return reviewQuery(null)
        .where(titleMatches.not().or(post.title.isNull()), bodyMatches)
        .orderBy(post.id.desc())
        .limit(limit)
        .fetch();
  }

  /** 조건이 참이면 1, 그 외에는 0인 SQL CASE 식을 만든다. Java에서 결과별 점수를 계산하지 않는다. */
  private NumberExpression<Integer> matchPriority(BooleanExpression matches) {
    return new CaseBuilder().when(matches).then(1).otherwise(0);
  }
}
