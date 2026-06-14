package com.stocat.amumal.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record UpdatePostRequest(
        @NotBlank String title,
        @NotBlank String content,
        @JsonProperty("attachFileUrl")
        String image
) {
}
