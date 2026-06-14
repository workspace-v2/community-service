package com.stocat.amumal.comment.service;

import com.stocat.amumal.comment.dto.CommentRequest;
import com.stocat.amumal.comment.dto.CommentResponse;

public interface CommentService {

    CommentResponse createComment(Long postId, Long userId, CommentRequest request);

    CommentResponse updateComment(Long postId, Long commentId, Long userId, CommentRequest request);
}
