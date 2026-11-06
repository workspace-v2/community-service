package com.stocat.amumal.post.service;

import static com.stocat.amumal.post.domain.QPost.post;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stocat.amumal.post.domain.Post;
import com.stocat.amumal.post.domain.ReviewTag;
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

  private JPAQuery<Post> base(Long menuId) {
    return queryFactory
        .selectFrom(post)
        .join(post.user)
        .fetchJoin()
        .join(post.menu)
        .fetchJoin()
        .join(post.menu.brand)
        .fetchJoin()
        .where(post.deletedAt.isNull(), menuId == null ? null : post.menu.id.eq(menuId));
  }

  public List<Post> findAllByOffset(long offset, long limit) {
    return base(null).orderBy(post.id.desc()).offset(offset).limit(limit).fetch();
  }

  public List<Post> findAllByCursor(Long menuId, Long cursor, long limit) {
    return base(menuId)
        .where(cursor == null ? null : post.id.lt(cursor))
        .orderBy(post.id.desc())
        .limit(limit)
        .fetch();
  }

  public List<Post> searchPosts(
      Long menuId, String keyword, long offset, long limit, PostSearchSort sort) {
    BooleanExpression matches =
        post.title
            .containsIgnoreCase(keyword)
            .or(post.content.containsIgnoreCase(keyword))
            .or(post.menu.name.containsIgnoreCase(keyword))
            .or(post.menu.brand.name.containsIgnoreCase(keyword));
    List<ReviewTag> tags = ReviewTag.matching(keyword);
    if (!tags.isEmpty()) matches = matches.or(post.tags.any().in(tags));
    JPAQuery<Post> q = base(menuId).where(matches).offset(offset).limit(limit);
    if (sort == PostSearchSort.RELEVANCE)
      q.orderBy(
          new CaseBuilder()
              .when(post.title.containsIgnoreCase(keyword))
              .then(1)
              .otherwise(0)
              .desc(),
          new CaseBuilder()
              .when(post.content.containsIgnoreCase(keyword))
              .then(1)
              .otherwise(0)
              .desc());
    return q.orderBy(post.id.desc()).fetch();
  }
}
