package com.stocat.amumal.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PostLikeResponse(
        @JsonProperty("post_id")
        Long postId,
        @JsonProperty("like_count")
        int likeCount
) {
}
