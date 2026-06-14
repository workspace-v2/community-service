package com.stocat.amumal.user.dto;

public record UpdateProfileResponse(
        Long userId,
        String nickname,
        String profileImageUrl
) {
}
