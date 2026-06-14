package com.stocat.amumal.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserResponse(
        Long userId,
        String email,
        String nickname,
        String profileImageUrl
) {
    @JsonProperty("idx")
    public Long idx() {
        return userId;
    }
}
