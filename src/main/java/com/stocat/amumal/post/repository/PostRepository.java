package com.stocat.amumal.post.repository;

import com.stocat.amumal.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    void deleteAllByUser_Id(@Param("userId") Long userId);
}
