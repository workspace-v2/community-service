package com.stocat.amumal.post.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.stocat.amumal.post.domain.Post;
import com.stocat.amumal.post.dto.PostCursorSliceResponse;
import com.stocat.amumal.post.dto.PostSearchSliceResponse;
import com.stocat.amumal.post.dto.PostSummaryResponse;
import com.stocat.amumal.post.repository.PostRepository;
import com.stocat.amumal.stock.domain.StockSnapshot;
import com.stocat.amumal.stock.domain.StockStatus;
import com.stocat.amumal.stock.repository.StockSnapshotRepository;
import com.stocat.amumal.user.domain.User;
import com.stocat.amumal.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
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
  @Autowired private StockSnapshotRepository stockSnapshotRepository;
  @Autowired private EntityManager entityManager;

  @Test
  @DisplayName("커서 페이지네이션은 중복과 누락 없이 마지막 페이지까지 조회한다")
  void getPostsByCursorReturnsStableSlicesWithoutGap() {
    User user =
        userRepository.save(
            User.of("cursor-writer@stocat.com", "Password1!", "cursor", "https://example.com"));
    stockSnapshotRepository.save(activeStock("005930", "삼성전자"));

    List<Long> expectedIds = new ArrayList<>();
    for (int i = 1; i <= 25; i++) {
      Post savedPost =
          postRepository.save(Post.of(user, "005930", "title-" + i, "content-" + i, null));
      expectedIds.add(savedPost.getId());
    }

    entityManager.flush();
    entityManager.clear();

    expectedIds.sort(Comparator.reverseOrder());

    PostCursorSliceResponse firstPage = postService.getPostsByCursor(null, null, 10);
    assertThat(firstPage.content()).hasSize(10);
    assertThat(firstPage.hasNext()).isTrue();
    assertThat(firstPage.nextCursor()).isEqualTo(firstPage.content().getLast().id());
    assertThat(extractIds(firstPage.content()))
        .containsExactlyElementsOf(expectedIds.subList(0, 10));
    assertSortedDescending(firstPage.content());

    PostCursorSliceResponse secondPage =
        postService.getPostsByCursor(null, firstPage.nextCursor(), 10);
    assertThat(secondPage.content()).hasSize(10);
    assertThat(secondPage.hasNext()).isTrue();
    assertThat(secondPage.nextCursor()).isEqualTo(secondPage.content().getLast().id());
    assertThat(extractIds(secondPage.content()))
        .containsExactlyElementsOf(expectedIds.subList(10, 20));
    assertThat(extractIds(firstPage.content()))
        .doesNotContainAnyElementsOf(extractIds(secondPage.content()));
    assertSortedDescending(secondPage.content());

    PostCursorSliceResponse thirdPage =
        postService.getPostsByCursor(null, secondPage.nextCursor(), 10);
    assertThat(thirdPage.content()).hasSize(5);
    assertThat(thirdPage.hasNext()).isFalse();
    assertThat(thirdPage.nextCursor()).isNull();
    assertThat(extractIds(thirdPage.content()))
        .containsExactlyElementsOf(expectedIds.subList(20, 25));
    assertThat(extractIds(secondPage.content()))
        .doesNotContainAnyElementsOf(extractIds(thirdPage.content()));
    assertSortedDescending(thirdPage.content());

    List<Long> actualIds = new ArrayList<>();
    actualIds.addAll(extractIds(firstPage.content()));
    actualIds.addAll(extractIds(secondPage.content()));
    actualIds.addAll(extractIds(thirdPage.content()));
    assertThat(actualIds).containsExactlyElementsOf(expectedIds);
  }

  @Test
  @DisplayName("커서 페이지네이션은 symbol 필터가 있으면 해당 종목 게시글만 조회한다")
  void getPostsByCursorFiltersBySymbol() {
    User user =
        userRepository.save(
            User.of(
                "symbol-cursor-writer@stocat.com",
                "Password1!",
                "symbolcur",
                "https://example.com"));
    stockSnapshotRepository.save(activeStock("005930", "삼성전자"));
    stockSnapshotRepository.save(activeStock("000660", "SK하이닉스"));

    Post samsung1 = postRepository.save(Post.of(user, "005930", "s-title-1", "content", null));
    postRepository.save(Post.of(user, "000660", "h-title-1", "content", null));
    Post samsung2 = postRepository.save(Post.of(user, "005930", "s-title-2", "content", null));

    entityManager.flush();
    entityManager.clear();

    PostCursorSliceResponse response = postService.getPostsByCursor("005930", null, 10);

    assertThat(extractIds(response.content())).containsExactly(samsung2.getId(), samsung1.getId());
    assertThat(response.content()).extracting(PostSummaryResponse::symbol).containsOnly("005930");
  }

  @Test
  @DisplayName("커서 페이지네이션은 없는 종목 코드면 빈 결과를 반환한다")
  void getPostsByCursorReturnsEmptyWhenSymbolDoesNotExist() {
    PostCursorSliceResponse response = postService.getPostsByCursor("999999", null, 10);

    assertThat(response.content()).isEmpty();
    assertThat(response.nextCursor()).isNull();
    assertThat(response.hasNext()).isFalse();
  }

  @Test
  @DisplayName("검색 페이지네이션은 hasNext와 offset 정보를 함께 반환한다")
  void searchPostsReturnsSliceMetadata() {
    User user =
        userRepository.save(
            User.of("search-slice@stocat.com", "Password1!", "srchslice", "https://example.com"));
    stockSnapshotRepository.save(activeStock("005930", "삼성전자"));

    for (int i = 1; i <= 11; i++) {
      postRepository.save(
          Post.of(user, "005930", "keyword title " + i, "keyword content " + i, null));
    }

    entityManager.flush();
    entityManager.clear();

    PostSearchSliceResponse response =
        postService.searchPosts(
            "005930", "keyword", 0, 10, com.stocat.amumal.post.dto.PostSearchSort.RECENT);

    assertThat(response.content()).hasSize(10);
    assertThat(response.offset()).isEqualTo(0);
    assertThat(response.limit()).isEqualTo(10);
    assertThat(response.hasNext()).isTrue();
    assertSortedDescending(response.content());
  }

  private List<Long> extractIds(List<PostSummaryResponse> content) {
    return content.stream().map(PostSummaryResponse::id).toList();
  }

  private void assertSortedDescending(List<PostSummaryResponse> content) {
    assertThat(content)
        .extracting(PostSummaryResponse::id)
        .isSortedAccordingTo(Comparator.reverseOrder());
  }

  private StockSnapshot activeStock(String symbol, String name) {
    LocalDateTime now = LocalDateTime.now();
    return StockSnapshot.of(symbol, name, "KOSPI", StockStatus.ACTIVE, null, null, now, now);
  }
}
