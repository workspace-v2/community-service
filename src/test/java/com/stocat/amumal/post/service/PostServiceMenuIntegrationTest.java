package com.stocat.amumal.post.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.stocat.amumal.menu.domain.Brand;
import com.stocat.amumal.menu.domain.Menu;
import com.stocat.amumal.menu.repository.BrandRepository;
import com.stocat.amumal.menu.repository.MenuRepository;
import com.stocat.amumal.post.domain.Post;
import com.stocat.amumal.post.repository.PostRepository;
import com.stocat.amumal.user.domain.User;
import com.stocat.amumal.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.profiles.include=")
@ActiveProfiles("test")
@Transactional
class PostServiceMenuIntegrationTest {

  @Autowired private PostService postService;
  @Autowired private UserRepository userRepository;
  @Autowired private PostRepository postRepository;
  @Autowired private MenuRepository menuRepository;
  @Autowired private BrandRepository brandRepository;
  private final Map<String, Menu> fixtures = new HashMap<>();
  @Autowired private EntityManager entityManager;

  @Test
  @DisplayName("게시글 목록 조회는 작성자 정보를 조회한다")
  void getPostsByOffsetReturnsReviews() {
    fixtures.put("cheese", menuRepository.save(activeMenu("합성 치즈버거")));

    for (int i = 1; i <= 10; i++) {
      User user =
          userRepository.save(
              User.of(
                  "nplus1-user-" + i + "@stocat.com",
                  "Password1!",
                  "writer" + i,
                  "https://example.com/profile-" + i));
      postRepository.save(
          Post.of(user, fixtures.get("cheese"), 4, Set.of(), "title-" + i, "content-" + i, null));
    }

    entityManager.flush();
    entityManager.clear();

    assertThat(postService.getPostsByOffset(0, 10)).hasSize(10);
  }

  @Test
  @DisplayName("커서 기반 게시글 목록 조회도 작성자 정보를 조회한다")
  void getPostsByCursorReturnsReviews() {
    fixtures.put("chicken", menuRepository.save(activeMenu("합성 치킨버거")));

    for (int i = 1; i <= 10; i++) {
      User user =
          userRepository.save(
              User.of(
                  "cursor-nplus1-user-" + i + "@stocat.com",
                  "Password1!",
                  "cwriter" + i,
                  "https://example.com/profile-" + i));
      postRepository.save(
          Post.of(user, fixtures.get("chicken"), 4, Set.of(), "title-" + i, "content-" + i, null));
    }

    entityManager.flush();
    entityManager.clear();

    assertThat(postService.getPostsByCursor(null, null, 10).content()).hasSize(10);
  }

  @Test
  @DisplayName("게시글 검색 조회 시 작성자 정보를 함께 조회한다")
  void searchPostsReturnsReviews() {
    fixtures.put("double", menuRepository.save(activeMenu("합성 더블버거")));

    User user =
        userRepository.save(
            User.of("search-writer@stocat.com", "Password1!", "searcher", "https://example.com"));
    for (int i = 1; i <= 10; i++) {
      postRepository.save(
          Post.of(
              user,
              fixtures.get("double"),
              4,
              Set.of(),
              "keyword title " + i,
              "keyword content " + i,
              null));
    }

    entityManager.flush();
    entityManager.clear();

    assertThat(postService.searchPosts(
        null, "keyword", 0, 10, com.stocat.amumal.post.dto.PostSearchSort.RECENT).content()).hasSize(10);
  }

  @Test
  @DisplayName("게시글 검색은 메뉴 필터가 있으면 해당 메뉴 리뷰만 조회한다")
  void searchPostsFiltersByMenu() {
    fixtures.put("double", menuRepository.save(activeMenu("합성 더블버거")));
    fixtures.put("spicy", menuRepository.save(activeMenu("합성 매콤버거")));

    User user =
        userRepository.save(
            User.of(
                "menu-search-writer@stocat.com",
                "Password1!",
                "menusearch",
                "https://example.com"));
    postRepository.save(
        Post.of(
            user, fixtures.get("double"), 4, Set.of(), "keyword double", "keyword content", null));
    postRepository.save(
        Post.of(
            user, fixtures.get("spicy"), 4, Set.of(), "keyword spicy", "keyword content", null));

    entityManager.flush();
    entityManager.clear();

    var results =
        postService.searchPosts(
            fixtures.get("double").getId(),
            "keyword",
            0,
            10,
            com.stocat.amumal.post.dto.PostSearchSort.RECENT);

    assertThat(results.content()).hasSize(1);
    assertThat(results.content().getFirst().menuId()).isEqualTo(fixtures.get("double").getId());
    assertThat(results.content().getFirst().title()).isEqualTo("keyword double");
  }

  @Test
  @DisplayName("게시글 검색은 없는 메뉴 ID면 빈 결과를 반환한다")
  void searchPostsReturnsEmptyWhenMenuDoesNotExist() {
    var results =
        postService.searchPosts(
            999999L, "keyword", 0, 10, com.stocat.amumal.post.dto.PostSearchSort.RECENT);

    assertThat(results.content()).isEmpty();
    assertThat(results.hasNext()).isFalse();
  }

  private Menu activeMenu(String name) {
    Brand brand = brandRepository.save(Brand.of("합성 브랜드"));
    return Menu.of(brand, name, "합성 메뉴 설명", 6000, Menu.SourceType.SYNTHETIC, null);
  }
}
