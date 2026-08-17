package com.stocat.amumal.post.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.stocat.amumal.post.domain.Post;
import com.stocat.amumal.post.repository.PostRepository;
import com.stocat.amumal.user.domain.User;
import com.stocat.amumal.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "spring.profiles.include=")
@ActiveProfiles("test")
class PostViewFlushServiceTest {

  @Autowired private PostViewFlushService postViewFlushService;
  @Autowired private PostViewService postViewService;
  @Autowired private TestPostViewService testPostViewService;
  @Autowired private PostRepository postRepository;
  @Autowired private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    testPostViewService.clear();
  }

  @Test
  @DisplayName("dirty post 조회수 delta를 DB에 flush하고 dirty 집합에서 제거한다")
  void flushDirtyPostViewCountsPersistsViewCountDelta() {
    Post post = savePost("flush-writer@stocat.com", "flushuser");

    postViewService.incrementViewCount(post.getId());
    postViewService.incrementViewCount(post.getId());
    postViewService.incrementViewCount(post.getId());

    int flushedPostCount = postViewFlushService.flushDirtyPostViewCounts(10);

    Post reloadedPost = postRepository.findById(post.getId()).orElseThrow();

    assertThat(flushedPostCount).isEqualTo(1);
    assertThat(reloadedPost.getViewCount()).isEqualTo(3L);
    assertThat(postViewService.getViewCountDelta(post.getId())).isZero();
    assertThat(postViewService.getDirtyPostIds(10)).doesNotContain(post.getId());
  }

  @Test
  @DisplayName("flush 중 새 조회수가 쌓이면 남은 delta를 다시 dirty 집합에 남긴다")
  void applyFlushedViewCountKeepsRemainingDeltaDirty() {
    Post post = savePost("remaining-writer@stocat.com", "remainusr");

    postViewService.incrementViewCount(post.getId());
    postViewService.incrementViewCount(post.getId());
    postViewService.incrementViewCount(post.getId());

    assertThat(postViewService.popDirtyPostIds(10)).containsExactly(post.getId());

    postViewService.incrementViewCount(post.getId());
    postViewService.incrementViewCount(post.getId());

    postViewService.applyFlushedViewCount(post.getId(), 3L);

    assertThat(postViewService.getViewCountDelta(post.getId())).isEqualTo(2L);
    assertThat(postViewService.getDirtyPostIds(10)).contains(post.getId());
  }

  private Post savePost(String email, String nickname) {
    User user = userRepository.save(User.of(email, "Password1!", nickname, "https://example.com"));
    return postRepository.save(Post.of(user, "title", "content", null));
  }
}
