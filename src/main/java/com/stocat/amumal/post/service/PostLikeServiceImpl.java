package com.stocat.amumal.post.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PostLikeServiceImpl implements PostLikeService {

  private final PostRepository postRepository;
  private final PostLikeRepository postLikeRepository;
  private final UserRepository userRepository;

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
    post.increaseLikeCount();

    return new PostLikeResponse(post.getId(), post.getLikeCount());
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
    post.decreaseLikeCount();

    return new PostLikeResponse(post.getId(), post.getLikeCount());
  }
}
