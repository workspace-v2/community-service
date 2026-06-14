package com.stocat.amumal.post.service;

import com.stocat.amumal.post.dto.CreatePostRequest;
import com.stocat.amumal.post.dto.CreatePostResponse;
import com.stocat.amumal.post.dto.GetPostResponse;
import com.stocat.amumal.post.dto.PostSearchSort;
import com.stocat.amumal.post.dto.PostSummaryResponse;
import com.stocat.amumal.post.dto.UpdatePostRequest;
import com.stocat.amumal.post.dto.UpdatePostResponse;
import java.util.List;

public interface PostService {

    CreatePostResponse createPost(Long userId, CreatePostRequest request);

    List<PostSummaryResponse> getPosts(int offset, int limit);

    List<PostSummaryResponse> searchPosts(String keyword, int offset, int limit, PostSearchSort sort);

    GetPostResponse getPost(Long postId, Long userId);

    UpdatePostResponse updatePost(Long postId, Long userId, UpdatePostRequest request);

    void deletePost(Long postId, Long userId);
}
