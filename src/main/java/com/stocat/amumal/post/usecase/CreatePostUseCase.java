package com.stocat.amumal.post.usecase;

import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.menu.domain.Menu;
import com.stocat.amumal.menu.repository.MenuRepository;
import com.stocat.amumal.post.domain.Post;
import com.stocat.amumal.post.dto.CreatePostRequest;
import com.stocat.amumal.post.dto.CreatePostResponse;
import com.stocat.amumal.post.repository.PostRepository;
import com.stocat.amumal.post.service.PostImageMappingService;
import com.stocat.amumal.post.validator.PostValidator;
import com.stocat.amumal.user.domain.User;
import com.stocat.amumal.user.repository.UserRepository;
import java.util.Set;
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
  private final MenuRepository menuRepository;

  @Transactional
  public CreatePostResponse execute(Long userId, CreatePostRequest request) {
    postValidator.validateCreatePost(request);

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    Menu menu =
        menuRepository
            .findById(request.menuId())
            .orElseThrow(() -> new ApiException(ErrorCode.MENU_NOT_FOUND));

    if (menu.isDeleted() || menu.getBrand().isDeleted())
      throw new ApiException(ErrorCode.MENU_NOT_FOUND);
    Post savedPost =
        postRepository.save(
            Post.of(
                user,
                menu,
                request.rating(),
                request.tags(),
                request.title().trim(),
                request.content().trim(),
                request.image() == null ? null : request.image().trim()));

    postImageMappingService.replace(savedPost, request.image());

    return new CreatePostResponse(
        savedPost.getId(),
        savedPost.getUser().getId(),
        menu.getId(),
        menu.getName(),
        menu.getBrand().getName(),
        savedPost.getRating(),
        Set.copyOf(savedPost.getTags()),
        savedPost.getTitle(),
        savedPost.getContent(),
        savedPost.getImageUrl());
  }
}
