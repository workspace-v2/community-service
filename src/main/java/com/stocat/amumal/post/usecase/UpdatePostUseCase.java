package com.stocat.amumal.post.usecase;

import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.post.domain.Post;
import com.stocat.amumal.post.dto.UpdatePostRequest;
import com.stocat.amumal.post.dto.UpdatePostResponse;
import com.stocat.amumal.post.repository.PostRepository;
import com.stocat.amumal.post.service.PostImageMappingService;
import com.stocat.amumal.post.validator.PostValidator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
public class UpdatePostUseCase {

    private final PostRepository postRepository;
    private final PostValidator postValidator;
    private final PostImageMappingService postImageMappingService;

    @Transactional
    public UpdatePostResponse execute(Long postId, Long userId, UpdatePostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

        postValidator.validateUpdatePost(request);

        if (!post.getUser().getId().equals(userId)) {
            throw new ApiException(ErrorCode.POST_UPDATE_FORBIDDEN);
        }

        post.update(
                request.title().trim(),
                request.content().trim(),
                request.image() == null ? null : request.image().trim()
        );

        postImageMappingService.replace(post, request.image());

        return new UpdatePostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getImageUrl()
        );
    }
}
