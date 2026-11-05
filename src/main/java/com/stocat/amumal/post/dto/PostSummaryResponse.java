package com.stocat.amumal.post.dto;

import com.stocat.amumal.post.domain.ReviewTag;
import java.util.Set;

public record PostSummaryResponse(
    Long id,
    Long menuId,
    String menuName,
    String brandName,
    Integer rating,
    Set<ReviewTag> tags,
    String title,
    String createdAt,
    int likeCount,
    int commentCount,
    long viewCount,
    AuthorResponse author) {
  public record AuthorResponse(Long userId, String nickname, String profileImageUrl) {}
}
