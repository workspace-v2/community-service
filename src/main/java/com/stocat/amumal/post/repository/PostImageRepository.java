package com.stocat.amumal.post.repository;

import com.stocat.amumal.post.domain.PostImage;
import com.stocat.amumal.post.domain.PostImageId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageRepository extends JpaRepository<PostImage, PostImageId> {
  List<PostImage> findById_PostId(Long postId);

  void deleteAllById_PostId(Long postId);
}
