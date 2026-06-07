package com.stocat.amumal.post.repository;

import com.stocat.amumal.post.domain.PostLike;
import com.stocat.amumal.post.domain.PostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {

    boolean existsById(PostLikeId id);

    long countById_PostId(Long postId);
}
