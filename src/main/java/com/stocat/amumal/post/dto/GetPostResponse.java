package com.stocat.amumal.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stocat.amumal.post.domain.ReviewTag;
import java.util.Set;

public record GetPostResponse(
    Long id,
    Long menuId,
    String menuName,
    String brandName,
    Integer rating,
    Set<ReviewTag> tags,
    Long userId,
    Long writerId,
    String title,
    String content,
    String nickname,
    String profileImage,
    String createdAt,
    long viewCount,
    int likeCount,
    int commentCount,
    boolean isLiked,
    String fileUrl) {
  @JsonProperty("filePath")
  public String filePath() {
    return fileUrl;
  }
}
