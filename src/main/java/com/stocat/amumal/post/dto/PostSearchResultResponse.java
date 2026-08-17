package com.stocat.amumal.post.dto;

public record PostSearchResultResponse(
    Long id,
    String symbol,
    String stockName,
    String market,
    String title,
    String createdAt,
    int likeCount,
    int commentCount,
    long viewCount,
    AuthorResponse author) {
  public record AuthorResponse(Long userId, String nickname, String profileImageUrl) {}
}
