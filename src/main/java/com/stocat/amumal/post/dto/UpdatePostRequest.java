package com.stocat.amumal.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/** 게시글 수정 요청. 종목(symbol)은 생성 이후 변경할 수 없다. */
public record UpdatePostRequest(
    @NotBlank String title,
    @NotBlank String content,
    @JsonProperty("attachFileUrl") String image) {}
