package com.stocat.amumal.comment.service;

import com.stocat.amumal.comment.domain.Comment;
import com.stocat.amumal.comment.dto.CommentAuthorResponse;
import com.stocat.amumal.comment.dto.CommentRequest;
import com.stocat.amumal.comment.dto.CommentResponse;
import com.stocat.amumal.comment.repository.CommentRepository;
import com.stocat.amumal.comment.validator.CommentValidator;
import com.stocat.amumal.common.DateTimeConstants;
import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.post.domain.Post;
import com.stocat.amumal.post.repository.PostRepository;
import com.stocat.amumal.user.domain.User;
import com.stocat.amumal.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

  private final CommentRepository commentRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final CommentValidator commentValidator;

  @Override
  @Transactional
  public CommentResponse createComment(Long postId, Long userId, CommentRequest request) {
    String content = commentValidator.normalizeContent(request.commentContent());

    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

    Comment savedComment = commentRepository.save(Comment.of(post, user, content));
    post.increaseCommentCount();

    return new CommentResponse(
        savedComment.getId(),
        savedComment.getPost().getId(),
        savedComment.getContent(),
        savedComment.getCreatedAt().format(DateTimeConstants.DATE_TIME_FORMATTER),
        new CommentAuthorResponse(
            savedComment.getUser().getId(),
            savedComment.getUser().getNickname(),
            savedComment.getUser().getProfileImageUrl()));
  }

  @Override
  @Transactional(readOnly = true)
  public List<CommentResponse> getComments(Long postId, int offset, int limit) {
    postRepository.findById(postId).orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

    commentValidator.validatePagination(offset, limit);
    PageRequest pageRequest = PageRequest.of(offset / limit, limit);

    return commentRepository.findAllByPost_IdOrderByCreatedAtAsc(postId, pageRequest).stream()
        .map(this::toCommentResponse)
        .toList();
  }

  @Override
  @Transactional
  public CommentResponse updateComment(
      Long postId, Long commentId, Long userId, CommentRequest request) {
    String content = commentValidator.normalizeContent(request.commentContent());

    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new ApiException(ErrorCode.COMMENT_NOT_FOUND));

    if (!comment.getPost().getId().equals(postId)) {
      throw new ApiException(ErrorCode.COMMENT_NOT_FOUND);
    }

    if (!comment.getUser().getId().equals(userId)) {
      throw new ApiException(ErrorCode.COMMENT_UPDATE_FORBIDDEN);
    }

    comment.update(content);

    return new CommentResponse(
        comment.getId(),
        comment.getPost().getId(),
        comment.getContent(),
        comment.getCreatedAt().format(DateTimeConstants.DATE_TIME_FORMATTER),
        new CommentAuthorResponse(
            comment.getUser().getId(),
            comment.getUser().getNickname(),
            comment.getUser().getProfileImageUrl()));
  }

  @Override
  @Transactional
  public void deleteComment(Long postId, Long commentId, Long userId) {
    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new ApiException(ErrorCode.COMMENT_NOT_FOUND));

    if (!comment.getPost().getId().equals(postId)) {
      throw new ApiException(ErrorCode.COMMENT_NOT_FOUND);
    }

    if (!comment.getUser().getId().equals(userId)) {
      throw new ApiException(ErrorCode.COMMENT_DELETE_FORBIDDEN);
    }

    comment.getPost().decreaseCommentCount();
    commentRepository.delete(comment);
  }

  private CommentResponse toCommentResponse(Comment comment) {
    return new CommentResponse(
        comment.getId(),
        comment.getPost().getId(),
        comment.getContent(),
        comment.getCreatedAt().format(DateTimeConstants.DATE_TIME_FORMATTER),
        new CommentAuthorResponse(
            comment.getUser().getId(),
            comment.getUser().getNickname(),
            comment.getUser().getProfileImageUrl()));
  }
}
