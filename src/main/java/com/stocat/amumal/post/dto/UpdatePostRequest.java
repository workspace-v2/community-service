package com.stocat.amumal.post.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdatePostRequest(
        @NotBlank String title,
        @NotBlank String content,
        String image
) {
}
