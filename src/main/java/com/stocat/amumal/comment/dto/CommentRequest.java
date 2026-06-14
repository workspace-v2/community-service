package com.stocat.amumal.comment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CommentRequest(
        @JsonProperty("commentContent")
        String commentContent
) {
}
