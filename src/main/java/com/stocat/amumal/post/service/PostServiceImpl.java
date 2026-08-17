package com.stocat.amumal.post.service;

import com.stocat.amumal.common.DateTimeConstants;
import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.post.domain.Post;
import com.stocat.amumal.post.domain.PostLikeId;
import com.stocat.amumal.post.dto.GetPostResponse;
import com.stocat.amumal.post.dto.PostCursorSliceResponse;
import com.stocat.amumal.post.dto.PostSearchSort;
import com.stocat.amumal.post.dto.PostStockResponse;
import com.stocat.amumal.post.dto.PostSummaryResponse;
import com.stocat.amumal.post.event.PostViewEventPublisher;
import com.stocat.amumal.post.repository.PostLikeRepository;
import com.stocat.amumal.post.repository.PostRepository;
import com.stocat.amumal.post.validator.PostValidator;
import com.stocat.amumal.user.repository.UserRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PostServiceImpl implements PostService {

  private final PostRepository postRepository;
  private final PostLikeRepository postLikeRepository;
  private final PostQuerydslService postQuerydslService;
  private final UserRepository userRepository;
  private final PostValidator postValidator;
  private final PostViewService postViewService;
  private final PostViewEventPublisher postViewEventPublisher;
  private final PostStockSnapshotService postStockSnapshotService;

  // DB 기준 조회수에 Redis delta를 더해 현재 표시값을 계산
  private long getViewCount(Post post) {
    return post.getViewCount() + postViewService.getViewCountDelta(post.getId());
  }

  @Override
  @Transactional(readOnly = true)
  public List<PostSummaryResponse> getPostsByOffset(int offset, int limit) {
    postValidator.validateListSize(limit);

    return toPostSummaryResponses(postQuerydslService.findAllByOffset(offset, limit));
  }

  @Override
  @Transactional(readOnly = true)
  public PostCursorSliceResponse getPostsByCursor(String symbol, Long cursor, int limit) {
    postValidator.validateListSize(limit);

    List<Post> posts = postQuerydslService.findAllByCursor(symbol, cursor, limit + 1);
    boolean hasNext = posts.size() > limit;
    List<Post> pagePosts = hasNext ? posts.subList(0, limit) : posts;
    Long nextCursor = hasNext ? pagePosts.getLast().getId() : null;

    return new PostCursorSliceResponse(toPostSummaryResponses(pagePosts), nextCursor, hasNext);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PostSummaryResponse> searchPosts(
      String symbol, String keyword, int offset, int limit, PostSearchSort sort) {
    postValidator.validateListSize(limit);

    return toPostSummaryResponses(
        postQuerydslService.searchPosts(symbol, keyword, offset, limit, sort));
  }

  @Override
  @Transactional(readOnly = true)
  public GetPostResponse getPost(Long postId, Long userId) {
    userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

    postViewEventPublisher.publishEvent(post.getId());

    PostStockResponse stock =
        post.getSymbol() == null
            ? postStockSnapshotService.emptyStockResponse(null)
            : postStockSnapshotService
                .getExistingStockResponses(Collections.singletonList(post.getSymbol()))
                .getOrDefault(
                    post.getSymbol(),
                    postStockSnapshotService.emptyStockResponse(post.getSymbol()));
    boolean isLiked = postLikeRepository.existsById(new PostLikeId(postId, userId));

    return new GetPostResponse(
        post.getId(),
        stock.symbol(),
        stock.stockName(),
        stock.market(),
        post.getUser().getId(),
        post.getUser().getId(),
        post.getTitle(),
        post.getContent(),
        post.getUser().getNickname(),
        post.getUser().getProfileImageUrl(),
        post.getCreatedAt().format(DateTimeConstants.DATE_TIME_FORMATTER),
        getViewCount(post),
        post.getLikeCount(),
        post.getCommentCount(),
        isLiked,
        post.getImageUrl());
  }

  @Override
  @Transactional
  public void deletePost(Long postId, Long userId) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

    if (!post.getUser().getId().equals(userId)) {
      throw new ApiException(ErrorCode.POST_DELETE_FORBIDDEN);
    }

    postRepository.delete(post);
  }

  private List<PostSummaryResponse> toPostSummaryResponses(List<Post> posts) {
    Map<String, PostStockResponse> stocks =
        postStockSnapshotService.getExistingStockResponses(
            posts.stream().map(Post::getSymbol).toList());

    return posts.stream().map(post -> toPostSummaryResponse(post, stocks)).toList();
  }

  private PostSummaryResponse toPostSummaryResponse(
      Post post, Map<String, PostStockResponse> stocks) {
    PostStockResponse stock =
        post.getSymbol() == null
            ? postStockSnapshotService.emptyStockResponse(null)
            : stocks.getOrDefault(
                post.getSymbol(), postStockSnapshotService.emptyStockResponse(post.getSymbol()));

    return new PostSummaryResponse(
        post.getId(),
        stock.symbol(),
        stock.stockName(),
        stock.market(),
        post.getTitle(),
        post.getCreatedAt().format(DateTimeConstants.DATE_TIME_FORMATTER),
        post.getLikeCount(),
        post.getCommentCount(),
        getViewCount(post),
        new PostSummaryResponse.AuthorResponse(
            post.getUser().getId(),
            post.getUser().getNickname(),
            post.getUser().getProfileImageUrl()));
  }
}
