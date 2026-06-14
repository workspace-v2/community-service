package com.stocat.amumal.post.dto;

public record UpdatePostResponse(
        Long id,
        String title,
        String content,
        String fileUrl
) {
}
