package com.stocat.amumal.post.service;

import com.stocat.amumal.common.DateTimeConstants;
import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.post.domain.Post;
import com.stocat.amumal.post.domain.PostLikeId;
import com.stocat.amumal.post.dto.GetPostResponse;
import com.stocat.amumal.post.dto.PostCursorSliceResponse;
import com.stocat.amumal.post.dto.PostSearchSliceResponse;
import com.stocat.amumal.post.dto.PostSearchSort;
import com.stocat.amumal.post.dto.PostSummaryResponse;
import com.stocat.amumal.post.event.PostViewEventPublisher;
import com.stocat.amumal.post.repository.PostLikeRepository;
import com.stocat.amumal.post.repository.PostRepository;
import com.stocat.amumal.post.validator.PostValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PostServiceImpl implements PostService {

  private final PostRepository postRepository;
  private final PostLikeRepository postLikeRepository;
  private final PostQuerydslService postQuerydslService;
  private final PostValidator postValidator;
  private final PostViewService postViewService;
  private final PostViewEventPublisher postViewEventPublisher;

  // DB 기준 조회수에 Redis delta를 더해 현재 표시값을 계산
  private long getViewCount(Post post) {
    return post.getViewCount() + postViewService.getViewCountDelta(post.getId());
  }

  @Override
  @Transactional(readOnly = true)
  public List<PostSummaryResponse> getPostsByOffset(int offset, int limit) {
    postValidator.validateListSize(limit);
    if (offset < 0 || offset > 10000) throw new ApiException(ErrorCode.INVALID_SEARCH);

    return toPostSummaryResponses(postQuerydslService.findAllByOffset(offset, limit));
  }

  @Override
  @Transactional(readOnly = true)
  public PostCursorSliceResponse getPostsByCursor(Long menuId, Long cursor, int limit) {
    postValidator.validateListSize(limit);

    List<Post> posts = postQuerydslService.findAllByCursor(menuId, cursor, limit + 1);
    boolean hasNext = posts.size() > limit;
    List<Post> pagePosts = hasNext ? posts.subList(0, limit) : posts;
    Long nextCursor = hasNext ? pagePosts.getLast().getId() : null;

    return new PostCursorSliceResponse(toPostSummaryResponses(pagePosts), nextCursor, hasNext);
  }

  @Override
  @Transactional(readOnly = true)
  public PostSearchSliceResponse searchPosts(
      Long menuId, String keyword, int offset, int limit, PostSearchSort sort) {
    postValidator.validateListSize(limit);

    postValidator.validateSearch(keyword, offset, limit);
    keyword = keyword.trim();
    List<Post> posts = postQuerydslService.searchPosts(menuId, keyword, offset, limit + 1, sort);
    boolean hasNext = posts.size() > limit;
    List<Post> pagePosts = hasNext ? posts.subList(0, limit) : posts;

    return new PostSearchSliceResponse(toPostSummaryResponses(pagePosts), offset, limit, hasNext);
  }

  @Override
  @Transactional(readOnly = true)
  public GetPostResponse getPost(Long postId, Long userId) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

    if (post.getMenu() == null || post.isDeleted())
      throw new ApiException(ErrorCode.POST_NOT_FOUND);
    postViewEventPublisher.publishEvent(post.getId());

    boolean isLiked =
        userId != null && postLikeRepository.existsById(new PostLikeId(postId, userId));

    return new GetPostResponse(
        post.getId(),
        post.getMenu().getId(),
        post.getMenu().getName(),
        post.getMenu().getBrand().getName(),
        post.getRating(),
        java.util.Set.copyOf(post.getTags()),
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
    return posts.stream().map(this::toPostSummaryResponse).toList();
  }

  private PostSummaryResponse toPostSummaryResponse(Post post) {
    return new PostSummaryResponse(
        post.getId(),
        post.getMenu().getId(),
        post.getMenu().getName(),
        post.getMenu().getBrand().getName(),
        post.getRating(),
        java.util.Set.copyOf(post.getTags()),
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
