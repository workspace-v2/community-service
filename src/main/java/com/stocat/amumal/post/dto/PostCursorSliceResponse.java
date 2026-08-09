package com.stocat.amumal.post.dto;

import java.util.List;

public record PostCursorSliceResponse(
    List<PostSummaryResponse> content, Long nextCursor, boolean hasNext) {}
