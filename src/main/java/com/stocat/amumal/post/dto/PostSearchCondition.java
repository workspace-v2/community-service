package com.stocat.amumal.post.dto;

public record PostSearchCondition(
        String title,
        String content,
        Long userId
) {
}
