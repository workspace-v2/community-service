package com.stocat.amumal.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GetPostsResponse(
    List<PostSummaryResponse> posts,
    @JsonProperty("has_next") boolean hasNext,
    @JsonProperty("next_cursor") Long nextCursor // 프론트는 이 값을 다음 요청에 cursor에 사용 가능
    ) {}
