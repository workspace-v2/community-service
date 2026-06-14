package com.stocat.amumal.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SignUpRequest(
        String email,
        String password,
        String passwordConfirm,
        String nickname,
        @JsonProperty("profileImageUrl")
        String profileImage
) {
}
