package com.stocat.amumal.post.dto;

public record UpdatePostResponse(
    Long id,
    String symbol,
    String stockName,
    String market,
    String title,
    String content,
    String fileUrl) {}
