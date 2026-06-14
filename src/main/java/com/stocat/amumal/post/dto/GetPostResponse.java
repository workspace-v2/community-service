package com.stocat.amumal.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GetPostResponse(
        Long id,
        Long userId,
        Long writerId,
        String title,
        String content,
        String nickname,
        String profileImage,
        String createdAt,
        int viewCount,
        int likeCount,
        int commentCount,
        boolean isLiked,
        String fileUrl
) {
    @JsonProperty("filePath")
    public String filePath() {
        return fileUrl;
    }
}
