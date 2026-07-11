package com.stocat.amumal.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreatePostResponse(
    @JsonProperty("insertId") Long id, Long userId, String title, String content, String fileUrl) {}
