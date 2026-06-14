package com.stocat.amumal.comment.validator;

import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class CommentValidator {

    private static final int MAX_COMMENT_LENGTH = 1500;

    public String normalizeContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new ApiException(ErrorCode.EMPTY_COMMENT_CONTENT);
        }

        String trimmed = content.trim();
        if (trimmed.length() > MAX_COMMENT_LENGTH) {
            throw new ApiException(ErrorCode.COMMENT_CONTENT_TOO_LONG);
        }
        return trimmed;
    }
}
