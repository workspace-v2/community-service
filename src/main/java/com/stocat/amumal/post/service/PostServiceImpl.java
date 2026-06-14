package com.stocat.amumal.post.service;

import com.stocat.amumal.common.DateTimeConstants;
import com.stocat.amumal.common.config.CacheConfig;
import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.post.domain.Post;
import com.stocat.amumal.post.domain.PostLikeId;
import com.stocat.amumal.post.dto.CreatePostRequest;
import com.stocat.amumal.post.dto.CreatePostResponse;
import com.stocat.amumal.post.dto.GetPostResponse;
import com.stocat.amumal.post.dto.PostSummaryResponse;
import com.stocat.amumal.post.dto.UpdatePostRequest;
import com.stocat.amumal.post.dto.UpdatePostResponse;
import com.stocat.amumal.post.event.PostViewEventPublisher;
import com.stocat.amumal.post.repository.PostLikeRepository;
import com.stocat.amumal.post.repository.PostRepository;
import com.stocat.amumal.post.validator.PostValidator;
import com.stocat.amumal.user.domain.User;
import com.stocat.amumal.user.repository.UserRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostQuerydslService postQuerydslService;
    private final UserRepository userRepository;
    private final PostValidator postValidator;
    private final CacheManager cacheManager;
    private final PostViewEventPublisher postViewEventPublisher;

    // 캐시에 누적된 delta와 DB 저장값을 합산해 반환
    private int getViewCount(Post post) {
        Cache cache = cacheManager.getCache(CacheConfig.CACHE_VIEW_COUNT);
        Cache.ValueWrapper wrapper = cache.get(post.getId());
        int delta = wrapper != null ? (int) wrapper.get() : 0;
        return post.getViewCount() + delta;
    }

    // 캐시 hit 시 캐시 값 반환, miss 시 post_like 테이블 COUNT로 복원 후 캐시에 올림 (read-through)
    private int getCachedLikeCount(Long postId) {
        Cache cache = cacheManager.getCache(CacheConfig.CACHE_LIKE_COUNT);
        Cache.ValueWrapper wrapper = cache.get(postId);
        if (wrapper != null) {
            return (int) wrapper.get();
        }
        int count = (int) postLikeRepository.countById_PostId(postId);
        cache.put(postId, count);
        return count;
    }

    @Override
    @Transactional
    public CreatePostResponse createPost(Long userId, CreatePostRequest request) {
        postValidator.validateCreatePost(request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        Post savedPost = postRepository.save(Post.of(
                user,
                request.title().trim(),
                request.content().trim(),
                request.image() == null ? null : request.image().trim()
        ));

        return new CreatePostResponse(
                savedPost.getId(),
                savedPost.getUser().getId(),
                savedPost.getTitle(),
                savedPost.getContent(),
                savedPost.getImageUrl()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostSummaryResponse> getPosts(int offset, int limit) {
        postValidator.validateListSize(limit);

        return postQuerydslService.findAllByOffset(offset, limit).stream()
                .map(this::toPostSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GetPostResponse getPost(Long postId, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

        postViewEventPublisher.publishEvent(post.getId());

        boolean isLiked = postLikeRepository.existsById(new PostLikeId(postId, userId));

        return new GetPostResponse(
                post.getId(),
                post.getUser().getId(),
                post.getUser().getId(),
                post.getTitle(),
                post.getContent(),
                post.getUser().getNickname(),
                post.getUser().getProfileImageUrl(),
                post.getCreatedAt().format(DateTimeConstants.DATE_TIME_FORMATTER),
                getViewCount(post),
                getCachedLikeCount(postId),
                post.getCommentCount(),
                isLiked,
                post.getImageUrl()
        );
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

        if (!post.getUser().getId().equals(userId)) {
            throw new ApiException(ErrorCode.POST_DELETE_FORBIDDEN);
        }

        postRepository.delete(post);
    }

    @Override
    @Transactional
    public UpdatePostResponse updatePost(Long postId, Long userId, UpdatePostRequest request) {
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

        return new UpdatePostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getImageUrl()
        );
    }

    private PostSummaryResponse toPostSummaryResponse(Post post) {
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getCreatedAt().format(DateTimeConstants.DATE_TIME_FORMATTER),
                getCachedLikeCount(post.getId()),
                post.getCommentCount(),
                getViewCount(post),
                new PostSummaryResponse.AuthorResponse(
                        post.getUser().getId(),
                        post.getUser().getNickname(),
                        post.getUser().getProfileImageUrl()
                )
        );
    }
}
