package com.stocat.amumal.post.dto;

import com.stocat.amumal.post.domain.ReviewTag;
import java.util.Set;

public record UpdatePostResponse(
    Long id,
    Long menuId,
    String menuName,
    String brandName,
    Integer rating,
    Set<ReviewTag> tags,
    String title,
    String content,
    String fileUrl) {}
