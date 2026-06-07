package com.stocat.amumal.post.service;

import com.stocat.amumal.common.DateTimeConstants;
import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.post.domain.Post;
import com.stocat.amumal.post.dto.CreatePostRequest;
import com.stocat.amumal.post.dto.CreatePostResponse;
import com.stocat.amumal.post.dto.GetPostResponse;
import com.stocat.amumal.post.dto.GetPostsResponse;
import com.stocat.amumal.post.dto.PostSummaryResponse;
import com.stocat.amumal.post.dto.UpdatePostRequest;
import com.stocat.amumal.post.dto.UpdatePostResponse;
import com.stocat.amumal.post.repository.PostRepository;
import com.stocat.amumal.post.validator.PostValidator;
import com.stocat.amumal.user.domain.User;
import com.stocat.amumal.user.repository.UserRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostValidator postValidator;

    public PostServiceImpl(PostRepository postRepository, UserRepository userRepository, PostValidator postValidator) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postValidator = postValidator;
    }

    @Override
    @Transactional
    public CreatePostResponse createPost(CreatePostRequest request) {
        postValidator.validateCreatePost(request);

        User user = userRepository.findById(request.userId())
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
        List<Post> posts = postRepository.findAllByCursor(cursor, PageRequest.of(0, size + 1));
        boolean hasNext = posts.size() > size;
        List<Post> pagePosts = hasNext ? posts.subList(0, size) : posts;

        List<PostSummaryResponse> postResponses = pagePosts.stream()
                .map(post -> new PostSummaryResponse(
                        post.getId(),
                        post.getTitle(),
                        post.getUser().getNickname(),
                        post.getCreatedAt().format(DateTimeConstants.DATE_TIME_FORMATTER),
                        0,  // TODO: PostLike 집계 후 대체
                        post.getCommentCount(),
                        0   // TODO: PostViewCount 집계 후 대체
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

        return new GetPostResponse(
                post.getId(),
                post.getUser().getId(),
                post.getTitle(),
                post.getContent(),
                post.getImageUrl(),
                post.getUser().getNickname(),
                post.getCreatedAt().format(DateTimeConstants.DATE_TIME_FORMATTER),
                0,  // TODO: PostViewCount 집계 후 대체
                0,  // TODO: PostLike 집계 후 대체
                post.getCommentCount(),
                false
        );
    }

    @Override
    @Transactional
    public UpdatePostResponse updatePost(Long postId, UpdatePostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

        postValidator.validateUpdatePost(request);

        if (!post.getUser().getId().equals(request.userId())) {
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
}
