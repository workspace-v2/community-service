package com.stocat.amumal.post.usecase;

import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.post.domain.Post;
import com.stocat.amumal.post.dto.PostStockResponse;
import com.stocat.amumal.post.dto.UpdatePostRequest;
import com.stocat.amumal.post.dto.UpdatePostResponse;
import com.stocat.amumal.post.repository.PostRepository;
import com.stocat.amumal.post.service.PostImageMappingService;
import com.stocat.amumal.post.service.PostStockSnapshotService;
import com.stocat.amumal.post.validator.PostValidator;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdatePostUseCase {

  private final PostRepository postRepository;
  private final PostValidator postValidator;
  private final PostImageMappingService postImageMappingService;
  private final PostStockSnapshotService postStockSnapshotService;

  @Transactional
  public UpdatePostResponse execute(Long postId, Long userId, UpdatePostRequest request) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

    postValidator.validateUpdatePost(request);

    if (!post.getUser().getId().equals(userId)) {
      throw new ApiException(ErrorCode.POST_UPDATE_FORBIDDEN);
    }

    post.update(
        request.title().trim(),
        request.content().trim(),
        request.image() == null ? null : request.image().trim());

    postImageMappingService.replace(post, request.image());
    PostStockResponse stock =
        post.getSymbol() == null
            ? postStockSnapshotService.emptyStockResponse(null)
            : postStockSnapshotService
                .getExistingStockResponses(Collections.singletonList(post.getSymbol()))
                .getOrDefault(
                    post.getSymbol(), postStockSnapshotService.emptyStockResponse(post.getSymbol()));

    return new UpdatePostResponse(
        post.getId(),
        stock.symbol(),
        stock.stockName(),
        stock.market(),
        post.getTitle(),
        post.getContent(),
        post.getImageUrl());
  }
}
