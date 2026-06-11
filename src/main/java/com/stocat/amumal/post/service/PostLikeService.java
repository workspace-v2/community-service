package com.stocat.amumal.post.service;

import com.stocat.amumal.post.dto.PostLikeResponse;

public interface PostLikeService {

    PostLikeResponse likePost(Long postId, Long userId);

    PostLikeResponse unlikePost(Long postId, Long userId);
}
