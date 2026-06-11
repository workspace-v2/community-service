package com.stocat.amumal.post.service;

import com.stocat.amumal.common.DateTimeConstants;
import com.stocat.amumal.common.config.CacheConfig;
import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.post.domain.Post;
import com.stocat.amumal.post.domain.PostLike;
import com.stocat.amumal.post.domain.PostLikeId;
import com.stocat.amumal.post.dto.CreatePostRequest;
import com.stocat.amumal.post.dto.CreatePostResponse;
import com.stocat.amumal.post.dto.GetPostResponse;
import com.stocat.amumal.post.dto.GetPostsResponse;
import com.stocat.amumal.post.dto.PostLikeResponse;
import com.stocat.amumal.post.dto.PostSummaryResponse;
import com.stocat.amumal.post.dto.UpdatePostRequest;
import com.stocat.amumal.post.dto.UpdatePostResponse;
import com.stocat.amumal.post.repository.PostLikeRepository;
import com.stocat.amumal.post.repository.PostRepository;
import com.stocat.amumal.post.validator.PostValidator;
import com.stocat.amumal.user.domain.User;
import com.stocat.amumal.user.repository.UserRepository;
import java.util.List;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostQuerydslService postQuerydslService;
    private final UserRepository userRepository;
    private final PostValidator postValidator;
    private final CacheManager cacheManager;

    public PostServiceImpl(PostRepository postRepository, PostLikeRepository postLikeRepository, PostQuerydslService postQuerydslService, UserRepository userRepository, PostValidator postValidator, CacheManager cacheManager) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.postQuerydslService = postQuerydslService;
        this.userRepository = userRepository;
        this.postValidator = postValidator;
        this.cacheManager = cacheManager;
    }

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

    // 캐시에 delta +1 누적
    public void incrementViewCountCache(Long postId) {
        Cache cache = cacheManager.getCache(CacheConfig.CACHE_VIEW_COUNT);
        Cache.ValueWrapper wrapper = cache.get(postId);
        int current = wrapper != null ? (int) wrapper.get() : 0;
        cache.put(postId, current + 1);
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
    public GetPostsResponse getPosts(Long cursor, int size) {
        postValidator.validateListSize(size);

        // 다음 페이지 존재 여부를 확인하기 위해 요청 개수보다 1개 더 조회
        List<Post> posts = postQuerydslService.findAllByCursor(cursor, size + 1);
        boolean hasNext = posts.size() > size;
        List<Post> pagePosts = hasNext ? posts.subList(0, size) : posts;

        List<PostSummaryResponse> postResponses = pagePosts.stream()
                .map(post -> new PostSummaryResponse(
                        post.getId(),
                        post.getTitle(),
                        post.getUser().getNickname(),
                        post.getCreatedAt().format(DateTimeConstants.DATE_TIME_FORMATTER),
                        getCachedLikeCount(post.getId()),
                        post.getCommentCount(),
                        getViewCount(post)
                ))
                .toList();

        Long nextCursor = hasNext && !pagePosts.isEmpty()
                ? pagePosts.get(pagePosts.size() - 1).getId()
                : null;

        return new GetPostsResponse(postResponses, hasNext, nextCursor);
    }

    @Override
    @Transactional(readOnly = true)
    public GetPostResponse getPost(Long postId, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

        incrementViewCountCache(postId);

        boolean isLiked = postLikeRepository.existsById(new PostLikeId(postId, userId));

        return new GetPostResponse(
                post.getId(),
                post.getUser().getId(),
                post.getTitle(),
                post.getContent(),
                post.getImageUrl(),
                post.getUser().getNickname(),
                post.getCreatedAt().format(DateTimeConstants.DATE_TIME_FORMATTER),
                getViewCount(post),
                getCachedLikeCount(postId),
                post.getCommentCount(),
                isLiked
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

    @Override
    @Transactional
    public PostLikeResponse likePost(Long postId, Long userId) {
        // 존재하는 게시글인지 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

        // 존재하는 사용자인지 확인
        User user = userRepository.findById(userId)
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
        int newCount = wrapper != null
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
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

        // 존재하는 유저인지 확인
        userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 취소할 좋아요가 있는지 확인
        PostLikeId likeId = new PostLikeId(postId, userId);
        if (!postLikeRepository.existsById(likeId)) {
            throw new ApiException(ErrorCode.POST_LIKE_NOT_FOUND);
        }

        postLikeRepository.deleteById(likeId);

        // 캐시가 이미 존재하면 해당 값에서 -1, 없으면 DB에 저장된 값을 가져옴
        Cache cache = cacheManager.getCache(CacheConfig.CACHE_LIKE_COUNT);
        Cache.ValueWrapper wrapper = cache.get(postId);
        int newCount = wrapper != null
                ? Math.max(0, (int) wrapper.get() - 1)
                : (int) postLikeRepository.countById_PostId(postId);
        cache.put(postId, newCount);

        return new PostLikeResponse(post.getId(), newCount);
    }
}
