package com.stocat.amumal.post.dto;

import java.util.List;

public record PostSearchSliceResponse(
    List<PostSummaryResponse> content, int offset, int limit, boolean hasNext) {}
