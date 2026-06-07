package com.stocat.amumal.post.repository;

import com.stocat.amumal.post.domain.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {
    // TODO: 추후 QueryDsl로 정의
//    List<Post> findAllByCursor(@Param("cursor") Long cursor, Pageable pageable);

    // TODO: 삭제, 일단 코드 에러때문에 생성
    List<Post> findAllByOrderByCreatedAtDesc();

    void deleteAllByUser_Id(@Param("userId") Long userId);
}
