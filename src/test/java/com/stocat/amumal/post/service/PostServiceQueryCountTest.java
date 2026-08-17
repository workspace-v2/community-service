package com.stocat.amumal.post.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.stocat.amumal.post.domain.Post;
import com.stocat.amumal.post.repository.PostRepository;
import com.stocat.amumal.stock.domain.StockSnapshot;
import com.stocat.amumal.stock.domain.StockStatus;
import com.stocat.amumal.stock.repository.StockSnapshotRepository;
import com.stocat.amumal.user.domain.User;
import com.stocat.amumal.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.profiles.include=")
@ActiveProfiles("test")
@Transactional
class PostServiceQueryCountTest {

  @Autowired private PostService postService;
  @Autowired private UserRepository userRepository;
  @Autowired private PostRepository postRepository;
  @Autowired private StockSnapshotRepository stockSnapshotRepository;
  @Autowired private EntityManager entityManager;
  @Autowired private EntityManagerFactory entityManagerFactory;

  @Test
  @DisplayName("게시글 목록 조회는 작성자 정보를 조회한다")
  void getPostsByOffsetFetchesAuthorsInSingleQuery() {
    stockSnapshotRepository.save(activeStock("005930", "삼성전자"));

    for (int i = 1; i <= 10; i++) {
      User user =
          userRepository.save(
              User.of(
                  "nplus1-user-" + i + "@stocat.com",
                  "Password1!",
                  "writer" + i,
                  "https://example.com/profile-" + i));
      postRepository.save(Post.of(user, "005930", "title-" + i, "content-" + i, null));
    }

    entityManager.flush();
    entityManager.clear();

    Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    statistics.clear();

    postService.getPostsByOffset(0, 10);
    assertThat(statistics.getPrepareStatementCount()).isEqualTo(2L);
  }

  @Test
  @DisplayName("커서 기반 게시글 목록 조회도 작성자 정보를 조회한다")
  void getPostsByOffsetByOffsetByCursorFetchesAuthorsInSingleQuery() {
    stockSnapshotRepository.save(activeStock("000660", "SK하이닉스"));

    for (int i = 1; i <= 10; i++) {
      User user =
          userRepository.save(
              User.of(
                  "cursor-nplus1-user-" + i + "@stocat.com",
                  "Password1!",
                  "cwriter" + i,
                  "https://example.com/profile-" + i));
      postRepository.save(Post.of(user, "000660", "title-" + i, "content-" + i, null));
    }

    entityManager.flush();
    entityManager.clear();

    Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    statistics.clear();

    postService.getPostsByCursor(null, 10);
    assertThat(statistics.getPrepareStatementCount()).isEqualTo(2L);
  }

  @Test
  @DisplayName("게시글 검색 조회 시 작성자 정보를 함께 조회한다")
  void searchPostsFetchesAuthorsInSingleQuery() {
    stockSnapshotRepository.save(activeStock("035420", "NAVER"));

    User user =
        userRepository.save(
            User.of("search-writer@stocat.com", "Password1!", "searcher", "https://example.com"));
    for (int i = 1; i <= 10; i++) {
      postRepository.save(Post.of(user, "035420", "keyword title " + i, "keyword content " + i, null));
    }

    entityManager.flush();
    entityManager.clear();

    Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    statistics.clear();

    postService.searchPosts("keyword", 0, 10, com.stocat.amumal.post.dto.PostSearchSort.RECENT);

    assertThat(statistics.getPrepareStatementCount()).isEqualTo(2L);
  }

  private StockSnapshot activeStock(String symbol, String name) {
    LocalDateTime now = LocalDateTime.now();
    return StockSnapshot.of(symbol, name, "KOSPI", StockStatus.ACTIVE, null, null, now, now);
  }
}
