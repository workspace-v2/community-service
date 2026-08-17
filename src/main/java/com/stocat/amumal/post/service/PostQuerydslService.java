package com.stocat.amumal.post.service;

import static com.stocat.amumal.post.domain.QPost.post;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stocat.amumal.post.domain.Post;
import com.stocat.amumal.post.dto.PostSearchSort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQuerydslService {

  private final JPAQueryFactory queryFactory;

  public List<Post> findAllByOffset(long offset, long limit) {
    return basePostListQuery(null).offset(offset).limit(limit).fetch();
  }

  public List<Post> findAllByCursor(String symbol, Long cursor, long limit) {
    return basePostListQuery(symbol).where(postIdLt(cursor)).limit(limit).fetch();
  }

  public List<Post> searchPosts(String keyword, long offset, long limit, PostSearchSort sort) {
    JPAQuery<Post> query =
        queryFactory
            .selectFrom(post)
            // 검색 결과도 동일한 DTO 매핑을 사용하므로 작성자를 함께 조회한다.
            .join(post.user)
            .fetchJoin()
            // 검색 조건에 맞는 게시글 집합을 먼저 만든 뒤, 그 결과에 offset/limit를 적용
            .where(keywordContains(keyword))
            .offset(offset)
            .limit(limit);

    if (sort == PostSearchSort.RELEVANCE) {
      // 제목 포함 > 본문 포함 > 최신순으로 우선 조회 되도록 함
      query.orderBy(
          post.title.containsIgnoreCase(keyword).desc(),
          post.content.containsIgnoreCase(keyword).desc(),
          post.id.desc());
    } else if (sort == PostSearchSort.RECENT) {
      query.orderBy(post.id.desc());
    } else {
      // 기본 정렬 최신순
      query.orderBy(post.id.desc());
    }

    return query.fetch();
  }

  private JPAQuery<Post> basePostListQuery(String symbol) {
    return queryFactory
        .selectFrom(post)
        // PostSummaryResponse 매핑 시 post.user 접근이 발생하므로 fetch join을 유지한다.
        .join(post.user)
        .fetchJoin()
        .where(symbolEq(symbol))
        .orderBy(post.id.desc());
  }

  private BooleanExpression postIdLt(Long cursor) {
    return cursor != null ? post.id.lt(cursor) : null;
  }

  private BooleanExpression symbolEq(String symbol) {
    return symbol != null && !symbol.isBlank() ? post.symbol.eq(symbol.trim()) : null;
  }

  private BooleanExpression keywordContains(String keyword) {
    // 검색어가 없으면 null을 반환하고, Querydsl은 이 조건을 where 절에서 제외함
    return keyword != null && !keyword.isBlank()
        ? post.title.containsIgnoreCase(keyword).or(post.content.containsIgnoreCase(keyword))
        : null;
  }
}
