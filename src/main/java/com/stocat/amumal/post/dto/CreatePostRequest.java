package com.stocat.amumal.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreatePostRequest(
    @NotBlank @Pattern(regexp = "\\d{6}") String symbol,
    @NotBlank String title,
    @NotBlank String content,
    @JsonProperty("attachFileUrl") String image) {}
