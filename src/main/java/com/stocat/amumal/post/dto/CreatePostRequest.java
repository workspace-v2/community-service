package com.stocat.amumal.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stocat.amumal.post.domain.ReviewTag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record CreatePostRequest(
    @NotNull @Positive Long menuId,
    @NotNull @Min(1) @Max(5) Integer rating,
    Set<@NotNull ReviewTag> tags,
    @NotBlank @Size(max = 26) String title,
    @NotBlank String content,
    @JsonProperty("attachFileUrl") String image) {
  public CreatePostRequest {
    if (tags == null) tags = Set.of();
  }
}
