package com.stocat.amumal.comment.controller;

import com.stocat.amumal.auth.annotation.AuthUserId;
import com.stocat.amumal.comment.dto.CommentRequest;
import com.stocat.amumal.comment.dto.CommentResponse;
import com.stocat.amumal.comment.service.CommentService;
import com.stocat.amumal.common.response.ApiResponse;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/{post_id}/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommentResponse> createComment(
            @Positive @PathVariable("post_id") Long postId,
            @AuthUserId Long userId,
            @RequestBody CommentRequest request
    ) {
        return ApiResponse.of("댓글 등록에 성공했습니다.", commentService.createComment(postId, userId, request));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<CommentResponse>> getComments(
            @Positive @PathVariable("post_id") Long postId,
            @PositiveOrZero @RequestParam(defaultValue = "0") int offset,
            @Positive @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.of("댓글 조회에 성공했습니다.", commentService.getComments(postId, offset, limit));
    }

    @PatchMapping("/{comment_id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<CommentResponse> updateComment(
            @Positive @PathVariable("post_id") Long postId,
            @Positive @PathVariable("comment_id") Long commentId,
            @AuthUserId Long userId,
            @RequestBody CommentRequest request
    ) {
        return ApiResponse.of("댓글 수정에 성공했습니다.",
                commentService.updateComment(postId, commentId, userId, request));
    }

    @DeleteMapping("/{comment_id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deleteComment(
            @Positive @PathVariable("post_id") Long postId,
            @Positive @PathVariable("comment_id") Long commentId,
            @AuthUserId Long userId
    ) {
        commentService.deleteComment(postId, commentId, userId);
        return ApiResponse.of("댓글 삭제에 성공했습니다.", null);
    }
}
