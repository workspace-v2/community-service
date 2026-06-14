package com.stocat.amumal.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record CreatePostRequest(
        @NotBlank String title,
        @NotBlank String content,
        @JsonProperty("attachFileUrl")
        String image
) {
}
