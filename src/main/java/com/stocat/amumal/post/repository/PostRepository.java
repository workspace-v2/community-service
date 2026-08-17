package com.stocat.amumal.post.repository;

import com.stocat.amumal.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

  void deleteAllByUser_Id(@Param("userId") Long userId);

  // Flush 배치에서는 Post를 로딩하지 않고 집계값만 직접 누적 반영 (카운터 집계이므로 update query 사용)
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update Post p set p.viewCount = p.viewCount + :delta where p.id = :postId")
  int incrementViewCount(@Param("postId") Long postId, @Param("delta") long delta);
}
