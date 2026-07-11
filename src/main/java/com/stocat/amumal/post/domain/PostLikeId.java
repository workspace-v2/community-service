package com.stocat.amumal.post.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable // 이 클래스가 다른 엔티티에 내장될 수 있음
@EqualsAndHashCode // JPA가 동일성 비교 시 사용
@NoArgsConstructor
public class PostLikeId implements Serializable {

  @Column(name = "post_id")
  private Long postId;

  @Column(name = "user_id")
  private Long userId;

  public PostLikeId(Long postId, Long userId) {
    this.postId = postId;
    this.userId = userId;
  }
}
