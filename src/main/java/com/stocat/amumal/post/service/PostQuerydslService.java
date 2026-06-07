package com.stocat.amumal.post.service;

import static com.stocat.amumal.post.domain.QPost.post;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stocat.amumal.post.domain.Post;
import com.stocat.amumal.post.dto.PostSearchCondition;
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
    public List<Post> findAllByCursor(Long cursor, int size) {
        return queryFactory
                .selectFrom(post)
                .where(cursorLt(cursor))
                .orderBy(post.id.desc())
                .limit(size)
                .fetch();
    }

    // 동적 검색: 모든 조건이 null이면 전체 조회, 조건이 있으면 AND로 조합
    public List<Post> searchPosts(PostSearchCondition condition) {
        return queryFactory
                .selectFrom(post)
                .where(
                        titleContains(condition.title()), // 제목 포함 검색 (null이면 조건 무시)
                        contentContains(condition.content()), // 내용 포함 검색 (null이면 조건 무시)
                        userIdEq(condition.userId()) // 작성자 필터 (null이면 조건 무시)
                )
                .orderBy(post.id.desc())
                .fetch();
    }

    // BooleanExpression 조건 메서드
    // 다양한 조건 조합 가능: null을 반환하면 where()가 해당 조건을 무시
    private BooleanExpression titleContains(String title) {
        return title != null ? post.title.containsIgnoreCase(title) : null;
    }

    private BooleanExpression contentContains(String content) {
        return content != null ? post.content.containsIgnoreCase(content) : null;
    }

    private BooleanExpression userIdEq(Long userId) {
        return userId != null ? post.user.id.eq(userId) : null;
    }

    private BooleanExpression cursorLt(Long cursor) {
        return cursor != null ? post.id.lt(cursor) : null;
    }

    @Transactional
    public void incrementViewCount(Long postId, int delta) {
        queryFactory
                .update(post)
                .set(post.viewCount, post.viewCount.add(delta))
                .where(post.id.eq(postId))
                .execute();
    }
}
