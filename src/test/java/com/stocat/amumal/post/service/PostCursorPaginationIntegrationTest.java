package com.stocat.amumal.post.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.stocat.amumal.post.domain.Post;
import com.stocat.amumal.post.dto.PostCursorSliceResponse;
import com.stocat.amumal.post.dto.PostSummaryResponse;
import com.stocat.amumal.post.repository.PostRepository;
import com.stocat.amumal.user.domain.User;
import com.stocat.amumal.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.profiles.include=")
@ActiveProfiles("test")
@Transactional
class PostCursorPaginationIntegrationTest {

  @Autowired private PostService postService;
  @Autowired private UserRepository userRepository;
  @Autowired private PostRepository postRepository;
  @Autowired private EntityManager entityManager;

  @Test
  @DisplayName("커서 페이지네이션은 중복과 누락 없이 마지막 페이지까지 조회한다")
  void getPostsByCursorReturnsStableSlicesWithoutGap() {
    User user =
        userRepository.save(
            User.of("cursor-writer@stocat.com", "Password1!", "cursor", "https://example.com"));

    List<Long> expectedIds = new ArrayList<>();
    for (int i = 1; i <= 25; i++) {
      Post savedPost = postRepository.save(Post.of(user, "title-" + i, "content-" + i, null));
      expectedIds.add(savedPost.getId());
    }

    entityManager.flush();
    entityManager.clear();

    expectedIds.sort(Comparator.reverseOrder());

    PostCursorSliceResponse firstPage = postService.getPostsByCursor(null, 10);
    assertThat(firstPage.content()).hasSize(10);
    assertThat(firstPage.hasNext()).isTrue();
    assertThat(firstPage.nextCursor()).isEqualTo(firstPage.content().getLast().id());
    assertThat(extractIds(firstPage.content())).containsExactlyElementsOf(expectedIds.subList(0, 10));
    assertSortedDescending(firstPage.content());

    PostCursorSliceResponse secondPage = postService.getPostsByCursor(firstPage.nextCursor(), 10);
    assertThat(secondPage.content()).hasSize(10);
    assertThat(secondPage.hasNext()).isTrue();
    assertThat(secondPage.nextCursor()).isEqualTo(secondPage.content().getLast().id());
    assertThat(extractIds(secondPage.content()))
        .containsExactlyElementsOf(expectedIds.subList(10, 20));
    assertThat(extractIds(firstPage.content())).doesNotContainAnyElementsOf(extractIds(secondPage.content()));
    assertSortedDescending(secondPage.content());

    PostCursorSliceResponse thirdPage = postService.getPostsByCursor(secondPage.nextCursor(), 10);
    assertThat(thirdPage.content()).hasSize(5);
    assertThat(thirdPage.hasNext()).isFalse();
    assertThat(thirdPage.nextCursor()).isNull();
    assertThat(extractIds(thirdPage.content()))
        .containsExactlyElementsOf(expectedIds.subList(20, 25));
    assertThat(extractIds(secondPage.content())).doesNotContainAnyElementsOf(extractIds(thirdPage.content()));
    assertSortedDescending(thirdPage.content());

    List<Long> actualIds = new ArrayList<>();
    actualIds.addAll(extractIds(firstPage.content()));
    actualIds.addAll(extractIds(secondPage.content()));
    actualIds.addAll(extractIds(thirdPage.content()));
    assertThat(actualIds).containsExactlyElementsOf(expectedIds);
  }

  private List<Long> extractIds(List<PostSummaryResponse> content) {
    return content.stream().map(PostSummaryResponse::id).toList();
  }

  private void assertSortedDescending(List<PostSummaryResponse> content) {
    assertThat(content)
        .extracting(PostSummaryResponse::id)
        .isSortedAccordingTo(Comparator.reverseOrder());
  }
}
