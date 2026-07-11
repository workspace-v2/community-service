package com.stocat.amumal.comment.service;

import com.stocat.amumal.comment.dto.CommentRequest;
import com.stocat.amumal.comment.dto.CommentResponse;
import java.util.List;

public interface CommentService {

  CommentResponse createComment(Long postId, Long userId, CommentRequest request);

  List<CommentResponse> getComments(Long postId, int offset, int limit);

  CommentResponse updateComment(Long postId, Long commentId, Long userId, CommentRequest request);

  void deleteComment(Long postId, Long commentId, Long userId);
}
