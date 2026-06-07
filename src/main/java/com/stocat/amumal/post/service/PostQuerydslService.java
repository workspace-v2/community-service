package com.stocat.amumal.post.service;

import static com.stocat.amumal.post.domain.QPost.post;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stocat.amumal.post.domain.Post;
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

    // BooleanExpression 조건 메서드
    private BooleanExpression cursorLt(Long cursor) {
        return cursor != null ? post.id.lt(cursor) : null;
    }
}
