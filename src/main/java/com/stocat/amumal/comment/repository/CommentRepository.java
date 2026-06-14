package com.stocat.amumal.comment.repository;

import com.stocat.amumal.comment.domain.Comment;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 댓글 조회 시 연관된 게시글(post)과 작성자(user)를 함께 조회-> 지연 로딩으로 인한 N+1 문제를 방지
    @EntityGraph(attributePaths = {"post", "user"})
    List<Comment> findAllByPost_IdOrderByCreatedAtAsc(Long postId, Pageable pageable);
}
