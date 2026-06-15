package com.stocat.amumal.post.controller;

import com.stocat.amumal.auth.annotation.AuthUserId;
import com.stocat.amumal.common.response.ApiResponse;
import com.stocat.amumal.image.dto.PostFileUploadResponse;
import com.stocat.amumal.post.dto.CreatePostRequest;
import com.stocat.amumal.post.dto.CreatePostResponse;
import com.stocat.amumal.post.dto.GetPostResponse;
import com.stocat.amumal.post.dto.PostLikeResponse;
import com.stocat.amumal.post.dto.PostSearchSort;
import com.stocat.amumal.post.dto.PostSummaryResponse;
import com.stocat.amumal.post.dto.UpdatePostRequest;
import com.stocat.amumal.post.dto.UpdatePostResponse;
import com.stocat.amumal.post.service.PostLikeService;
import com.stocat.amumal.post.service.PostService;
import com.stocat.amumal.post.usecase.CreatePostUseCase;
import com.stocat.amumal.post.usecase.UpdatePostUseCase;
import com.stocat.amumal.post.usecase.UploadPostImageUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/posts")
public class PostController {

    private final UploadPostImageUseCase uploadPostImageUseCase;
    private final PostService postService;
    private final PostLikeService postLikeService;
    private final CreatePostUseCase createPostUseCase;
    private final UpdatePostUseCase updatePostUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreatePostResponse> createPost(
            @AuthUserId Long userId,
            @Valid @RequestBody CreatePostRequest request
    ) {
        return ApiResponse.of("게시글이 생성되었습니다.", createPostUseCase.execute(userId, request));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<PostSummaryResponse>> getPosts(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @Positive @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ApiResponse.of("게시글 목록 조회에 성공했습니다.", postService.getPosts(offset, limit));
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<PostSummaryResponse>> searchPosts(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @Positive @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(value = "sort", defaultValue = "recent") String sort
    ) {
        PostSearchSort searchSort = PostSearchSort.from(sort);
        return ApiResponse.of("게시글 검색에 성공했습니다.",
                postService.searchPosts(keyword, offset, limit, searchSort));
    }

    @GetMapping("/{post_id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<GetPostResponse> getPost(
            @Positive @PathVariable("post_id") Long postId,
            @AuthUserId Long userId
    ) {
        return ApiResponse.of("게시글 조회에 성공했습니다.", postService.getPost(postId, userId));
    }

    @DeleteMapping("/{post_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(
            @Positive @PathVariable("post_id") Long postId,
            @AuthUserId Long userId
    ) {
        postService.deletePost(postId, userId);
    }

    @PatchMapping("/{post_id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UpdatePostResponse> updatePost(
            @Positive @PathVariable("post_id") Long postId,
            @AuthUserId Long userId,
            @Valid @RequestBody UpdatePostRequest request
    ) {
        return ApiResponse.of("게시글이 수정되었습니다.", updatePostUseCase.execute(postId, userId, request));
    }

    @PostMapping("/{post_id}/likes")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostLikeResponse> likePost(
            @Positive @PathVariable("post_id") Long postId,
            @AuthUserId Long userId
    ) {
        return ApiResponse.of("좋아요가 등록되었습니다.", postLikeService.likePost(postId, userId));
    }

    @DeleteMapping("/{post_id}/likes")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PostLikeResponse> unlikePost(
            @Positive @PathVariable("post_id") Long postId,
            @AuthUserId Long userId
    ) {
        return ApiResponse.of("좋아요가 취소되었습니다.", postLikeService.unlikePost(postId, userId));
    }

    @PostMapping("/upload/attach-file")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostFileUploadResponse> uploadPostImage(
            @RequestParam("postFile") MultipartFile file
    ) {
        return ApiResponse.of("파일이 업로드되었습니다.", uploadPostImageUseCase.execute(file));
    }
}
