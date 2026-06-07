package com.stocat.amumal.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record UpdatePostRequest(
        @Positive @JsonProperty("user_id") Long userId,
        @NotBlank String title,
        @NotBlank String content,
        String image
) {
}
