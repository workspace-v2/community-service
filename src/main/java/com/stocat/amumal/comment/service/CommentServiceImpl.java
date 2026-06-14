package com.stocat.amumal.comment.service;

import com.stocat.amumal.comment.domain.Comment;
import com.stocat.amumal.comment.dto.CommentAuthorResponse;
import com.stocat.amumal.comment.dto.CommentRequest;
import com.stocat.amumal.comment.dto.CommentResponse;
import com.stocat.amumal.comment.repository.CommentRepository;
import com.stocat.amumal.common.DateTimeConstants;
import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.post.domain.Post;
import com.stocat.amumal.post.repository.PostRepository;
import com.stocat.amumal.user.domain.User;
import com.stocat.amumal.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private static final int MAX_COMMENT_LENGTH = 1500;

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CommentResponse createComment(Long postId, Long userId, CommentRequest request) {
        String content = normalizeContent(request.commentContent());

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));
        User user = userRepository.findById(userId)
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
                        savedComment.getUser().getProfileImageUrl()
                )
        );
    }

    private String normalizeContent(String content) {
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
