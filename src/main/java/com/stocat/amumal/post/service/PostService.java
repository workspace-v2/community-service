package com.stocat.amumal.post.service;

import com.stocat.amumal.post.dto.GetPostResponse;
import com.stocat.amumal.post.dto.PostCursorSliceResponse;
import com.stocat.amumal.post.dto.PostSearchSort;
import com.stocat.amumal.post.dto.PostSummaryResponse;
import java.util.List;

public interface PostService {

  List<PostSummaryResponse> getPostsByOffset(int offset, int limit);

  PostCursorSliceResponse getPostsByCursor(String symbol, Long cursor, int limit);

  List<PostSummaryResponse> searchPosts(
      String symbol, String keyword, int offset, int limit, PostSearchSort sort);

  GetPostResponse getPost(Long postId, Long userId);

  void deletePost(Long postId, Long userId);
}
