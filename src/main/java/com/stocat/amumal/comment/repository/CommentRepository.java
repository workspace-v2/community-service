package com.stocat.amumal.comment.repository;

import com.stocat.amumal.comment.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
