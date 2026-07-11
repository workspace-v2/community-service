package com.stocat.amumal.post.dto;

public record PostSummaryResponse(
    Long id,
    String title,
    String createdAt,
    int likeCount,
    int commentCount,
    int viewCount,
    AuthorResponse author) {
  public record AuthorResponse(Long userId, String nickname, String profileImageUrl) {}
}
