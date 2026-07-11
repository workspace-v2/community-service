package com.stocat.amumal.post.usecase;

import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.post.domain.Post;
import com.stocat.amumal.post.dto.CreatePostRequest;
import com.stocat.amumal.post.dto.CreatePostResponse;
import com.stocat.amumal.post.repository.PostRepository;
import com.stocat.amumal.post.service.PostImageMappingService;
import com.stocat.amumal.post.validator.PostValidator;
import com.stocat.amumal.user.domain.User;
import com.stocat.amumal.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CreatePostUseCase {

  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final PostValidator postValidator;
  private final PostImageMappingService postImageMappingService;

  @Transactional
  public CreatePostResponse execute(Long userId, CreatePostRequest request) {
    postValidator.validateCreatePost(request);

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

    Post savedPost =
        postRepository.save(
            Post.of(
                user,
                request.title().trim(),
                request.content().trim(),
                request.image() == null ? null : request.image().trim()));

    postImageMappingService.replace(savedPost, request.image());

    return new CreatePostResponse(
        savedPost.getId(),
        savedPost.getUser().getId(),
        savedPost.getTitle(),
        savedPost.getContent(),
        savedPost.getImageUrl());
  }
}
