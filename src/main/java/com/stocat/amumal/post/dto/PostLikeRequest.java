package com.stocat.amumal.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PostLikeRequest(
        @JsonProperty("user_id")
        Long userId
) {
}
