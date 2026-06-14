package com.stocat.amumal.comment.dto;

public record CommentResponse(
        Long id,
        Long postId,
        String content,
        String createdAt,
        CommentAuthorResponse author
) {
}
