package com.stocat.amumal.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stocat.amumal.post.domain.ReviewTag;
import java.util.Set;

public record CreatePostResponse(
    @JsonProperty("insertId") Long id,
    Long userId,
    Long menuId,
    String menuName,
    String brandName,
    Integer rating,
    Set<ReviewTag> tags,
    String title,
    String content,
    String fileUrl) {}
