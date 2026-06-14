package com.stocat.amumal.post.dto;

import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;

public enum PostSearchSort {
    RECENT("recent"),
    RELEVANCE("relevance");

    private final String value;

    PostSearchSort(String value) {
        this.value = value;
    }

    public static PostSearchSort from(String value) {
        for (PostSearchSort sort : values()) {
            if (sort.value.equalsIgnoreCase(value)) {
                return sort;
            }
        }
        throw new ApiException(ErrorCode.INVALID_POST_SEARCH_SORT);
    }
}
