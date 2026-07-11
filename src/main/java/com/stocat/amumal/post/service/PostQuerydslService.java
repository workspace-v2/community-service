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

  // cursor 기반 페이지네이션
  @Deprecated
  public List<Post> findAllByCursor(Long cursor, int size) {
    return queryFactory
        .selectFrom(post)
        .where(cursorLt(cursor))
        .orderBy(post.id.desc())
        .limit(size)
        .fetch();
  }

  public List<Post> findAllByOffset(long offset, long limit) {
    return queryFactory
        .selectFrom(post)
        .orderBy(post.id.desc())
        .offset(offset)
        .limit(limit)
        .fetch();
  }

  public List<Post> searchPosts(String keyword, long offset, long limit, PostSearchSort sort) {
    JPAQuery<Post> query =
        queryFactory
            .selectFrom(post)
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

  // BooleanExpression 조건 메서드
  // 다양한 조건 조합 가능: null을 반환하면 where()가 해당 조건을 무시
  private BooleanExpression cursorLt(Long cursor) {
    return cursor != null ? post.id.lt(cursor) : null;
  }

  private BooleanExpression keywordContains(String keyword) {
    // 검색어가 없으면 null을 반환하고, Querydsl은 이 조건을 where 절에서 제외함
    return keyword != null && !keyword.isBlank()
        ? post.title.containsIgnoreCase(keyword).or(post.content.containsIgnoreCase(keyword))
        : null;
  }
}
