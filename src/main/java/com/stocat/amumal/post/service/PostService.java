package com.stocat.amumal.post.service;

import com.stocat.amumal.post.dto.CreatePostRequest;
import com.stocat.amumal.post.dto.CreatePostResponse;
import com.stocat.amumal.post.dto.GetPostResponse;
import com.stocat.amumal.post.dto.GetPostsResponse;
import com.stocat.amumal.post.dto.UpdatePostRequest;
import com.stocat.amumal.post.dto.UpdatePostResponse;

public interface PostService {

    CreatePostResponse createPost(CreatePostRequest request);

    GetPostsResponse getPosts(Long cursor, int size);

    GetPostResponse getPost(Long postId, Long userId);

    UpdatePostResponse updatePost(Long postId, UpdatePostRequest request);

    void deletePost(Long postId, Long userId);
}
