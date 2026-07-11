package com.stocat.amumal.post.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
public class PostImageId implements Serializable {

  @Column(name = "post_id")
  private Long postId;

  @Column(name = "image_id")
  private Long imageId;

  public PostImageId(Long postId, Long imageId) {
    this.postId = postId;
    this.imageId = imageId;
  }
}
