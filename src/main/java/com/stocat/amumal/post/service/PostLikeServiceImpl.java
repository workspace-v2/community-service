package com.stocat.amumal.post.service;

import com.stocat.amumal.common.config.CacheConfig;
import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.post.domain.Post;
import com.stocat.amumal.post.domain.PostLike;
import com.stocat.amumal.post.domain.PostLikeId;
import com.stocat.amumal.post.dto.PostLikeResponse;
import com.stocat.amumal.post.repository.PostLikeRepository;
import com.stocat.amumal.post.repository.PostRepository;
import com.stocat.amumal.user.domain.User;
import com.stocat.amumal.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PostLikeServiceImpl implements PostLikeService {

  private final PostRepository postRepository;
  private final PostLikeRepository postLikeRepository;
  private final UserRepository userRepository;
  private final CacheManager cacheManager;

  @Override
  @Transactional
  public PostLikeResponse likePost(Long postId, Long userId) {
    // 존재하는 게시글인지 확인
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

    // 존재하는 사용자인지 확인
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

    // 이미 게시글 좋아요를 수행했는지 확인
    PostLikeId likeId = new PostLikeId(postId, userId);
    if (postLikeRepository.existsById(likeId)) {
      throw new ApiException(ErrorCode.POST_ALREADY_LIKED);
    }

    postLikeRepository.save(PostLike.of(post, user));

    // 게시글 좋아요 수 캐시 확인
    Cache cache = cacheManager.getCache(CacheConfig.CACHE_LIKE_COUNT);
    Cache.ValueWrapper wrapper = cache.get(postId);

    // 캐시가 이미 존재하면 해당 값에서 +1, 없으면 DB에 저장된 값을 가져옴
    int newCount =
        wrapper != null
            ? (int) wrapper.get() + 1
            : (int) postLikeRepository.countById_PostId(postId);

    // 캐시값 업데이트
    cache.put(postId, newCount);

    return new PostLikeResponse(post.getId(), newCount);
  }

  @Override
  @Transactional
  public PostLikeResponse unlikePost(Long postId, Long userId) {
    // 존재하는 게시글인지 확인
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

    // 존재하는 유저인지 확인
    userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

    // 취소할 좋아요가 있는지 확인
    PostLikeId likeId = new PostLikeId(postId, userId);
    if (!postLikeRepository.existsById(likeId)) {
      throw new ApiException(ErrorCode.POST_LIKE_NOT_FOUND);
    }

    postLikeRepository.deleteById(likeId);

    // 캐시가 이미 존재하면 해당 값에서 -1, 없으면 DB에 저장된 값을 가져옴
    Cache cache = cacheManager.getCache(CacheConfig.CACHE_LIKE_COUNT);
    Cache.ValueWrapper wrapper = cache.get(postId);
    int newCount =
        wrapper != null
            ? Math.max(0, (int) wrapper.get() - 1)
            : (int) postLikeRepository.countById_PostId(postId);
    cache.put(postId, newCount);

    return new PostLikeResponse(post.getId(), newCount);
  }
}
