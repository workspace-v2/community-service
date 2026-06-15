package com.stocat.amumal.post.service;

import com.stocat.amumal.post.dto.GetPostResponse;
import com.stocat.amumal.post.dto.PostSearchSort;
import com.stocat.amumal.post.dto.PostSummaryResponse;
import java.util.List;

public interface PostService {

    List<PostSummaryResponse> getPosts(int offset, int limit);

    List<PostSummaryResponse> searchPosts(String keyword, int offset, int limit, PostSearchSort sort);

    GetPostResponse getPost(Long postId, Long userId);

    void deletePost(Long postId, Long userId);
}
