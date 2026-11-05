package com.stocat.amumal.post;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.stocat.amumal.auth.JwtProvider;
import com.stocat.amumal.comment.domain.Comment;
import com.stocat.amumal.comment.repository.CommentRepository;
import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.menu.domain.*;
import com.stocat.amumal.menu.repository.*;
import com.stocat.amumal.menu.service.MenuService;
import com.stocat.amumal.post.domain.*;
import com.stocat.amumal.post.dto.*;
import com.stocat.amumal.post.repository.*;
import com.stocat.amumal.post.service.PostService;
import com.stocat.amumal.post.usecase.*;
import com.stocat.amumal.user.domain.User;
import com.stocat.amumal.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.Set;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.profiles.include=")
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc
class BurgerReviewIntegrationTest {
  @Autowired MenuRepository menus;
  @Autowired BrandRepository brands;
  @Autowired UserRepository users;
  @Autowired PostRepository posts;
  @Autowired PostService service;
  @Autowired MenuService catalog;
  @Autowired CreatePostUseCase create;
  @Autowired UpdatePostUseCase update;
  @Autowired EntityManager em;
  @Autowired JdbcTemplate jdbc;
  @Autowired MockMvc mvc;
  User author;
  Menu menu;

  @BeforeEach
  void seed() {
    author = users.save(User.of("burger-writer@example.com", "Password1!", "버거리뷰어", null));
    Brand brand = brands.save(Brand.of("합성 버거집"));
    menu =
        menus.save(Menu.of(brand, "합성 치즈버거", "두꺼운 패티와 치즈", 6000, Menu.SourceType.SYNTHETIC, null));
  }

  @Test
  void existingCrudPersistsMenuRatingAndTags() {
    var created =
        create.execute(
            author.getId(),
            new CreatePostRequest(
                menu.getId(),
                5,
                Set.of(ReviewTag.CHEESY, ReviewTag.THICK_PATTY),
                "첫 리뷰",
                "육즙이 풍부하다",
                null));
    Long id = created.id();
    em.flush();
    em.clear();
    var changed =
        update.execute(
            id,
            author.getId(),
            new UpdatePostRequest(3, Set.of(ReviewTag.GOOD_VALUE), "수정 리뷰", "가격이 괜찮다", null));
    assertThat(changed.menuId()).isEqualTo(menu.getId());
    em.flush();
    em.clear();
    var detail = service.getPost(id, author.getId());
    assertThat(detail.rating()).isEqualTo(3);
    assertThat(detail.tags()).containsExactly(ReviewTag.GOOD_VALUE);
    assertThat(service.searchPosts(menu.getId(), "가성비", 0, 10, PostSearchSort.RELEVANCE).content())
        .extracting(PostSummaryResponse::id)
        .containsExactly(id);
    service.deletePost(id, author.getId());
    em.flush();
    em.clear();
    assertThat(posts.existsById(id)).isFalse();
    assertThat(
            jdbc.queryForObject("select count(*) from post_tag where post_id=?", Integer.class, id))
        .isZero();
  }

  @Test
  void ownershipAndInvalidMenuOrRatingAreRejected() {
    var created =
        create.execute(
            author.getId(), new CreatePostRequest(menu.getId(), 4, Set.of(), "리뷰", "내용", null));
    assertThatThrownBy(
            () ->
                update.execute(
                    created.id(), -1L, new UpdatePostRequest(3, Set.of(), "수정", "내용", null)))
        .isInstanceOf(ApiException.class);
    assertThatThrownBy(() -> service.deletePost(created.id(), -1L))
        .isInstanceOf(ApiException.class);
    assertThatThrownBy(
            () ->
                create.execute(
                    author.getId(), new CreatePostRequest(999999L, 5, Set.of(), "리뷰", "내용", null)))
        .isInstanceOf(ApiException.class);
    assertThatThrownBy(
            () ->
                create.execute(
                    author.getId(),
                    new CreatePostRequest(menu.getId(), 0, Set.of(), "리뷰", "내용", null)))
        .isInstanceOf(ApiException.class);
  }

  @Test
  void catalogSupportsBrandPriceKeywordAndIndependentSlices() {
    Menu other =
        menus.save(
            Menu.of(menu.getBrand(), "합성 프리미엄버거", "매콤", 12000, Menu.SourceType.SYNTHETIC, null));
    assertThat(catalog.list("합성", menu.getBrand().getId(), 7000, 0, 10, false).content())
        .extracting(com.stocat.amumal.menu.dto.MenuResponse::id)
        .containsExactly(menu.getId());
    var first = catalog.list("합성", menu.getBrand().getId(), null, 0, 1, false);
    assertThat(first.hasNext()).isTrue();
    assertThat(first.content().getFirst().id()).isEqualTo(other.getId());
    assertThat(
            catalog
                .list("합성", menu.getBrand().getId(), null, 1, 1, false)
                .content()
                .getFirst()
                .id())
        .isEqualTo(menu.getId());
    assertThat(catalog.get(menu.getId()).sourceType()).isEqualTo(Menu.SourceType.SYNTHETIC);
  }

  @Test
  void legacyRowsArePreservedButExcludedFromBurgerListsAndDetail() {
    jdbc.update(
        "insert into post(user_id,title,content,comment_count,like_count,view_count,symbol) values(?,?,?,0,0,0,?)",
        author.getId(),
        "legacy",
        "legacy body",
        "005930");
    Long id = jdbc.queryForObject("select max(post_id) from post", Long.class);
    assertThat(service.getPostsByOffset(0, 100))
        .extracting(PostSummaryResponse::id)
        .doesNotContain(id);
    assertThatThrownBy(() -> service.getPost(id, null)).isInstanceOf(ApiException.class);
    assertThat(jdbc.queryForObject("select symbol from post where post_id=?", String.class, id))
        .isEqualTo("005930");
  }

  @Test
  // 기존 JWT 필터는 /posts 쓰기도 제외한다. 여기서는 이미 해석된 userId를 전달해 새 필드만 검증한다.
  void reviewControllerAcceptsNewFieldsWithResolvedUserId() throws Exception {
    String body =
        "{\"menuId\":"
            + menu.getId()
            + ",\"rating\":5,\"tags\":[\"CHEESY\"],\"title\":\"맛있는 버거\",\"content\":\"치즈가 풍부함\"}";
    mvc.perform(
            post("/v1/posts")
                .contextPath("/v1")
                .requestAttr("userId", author.getId())
                .contentType("application/json")
                .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("data.menuId").value(menu.getId()))
        .andExpect(jsonPath("data.rating").value(5));
    mvc.perform(
            post("/v1/posts")
                .contextPath("/v1")
                .requestAttr("userId", author.getId())
                .contentType("application/json")
                .content(body.replace("\"rating\":5", "\"rating\":6")))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/v1/posts")
                .contextPath("/v1")
                .requestAttr("userId", author.getId())
                .contentType("application/json")
                .content(body.replace("CHEESY", "UNKNOWN")))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/v1/posts/cursor").contextPath("/v1").param("menuId", "-1"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void publicCombinedSearchReturnsBothTargetsAndRejectsEmptyOrHugePages() throws Exception {
    create.execute(
        author.getId(),
        new CreatePostRequest(menu.getId(), 4, Set.of(ReviewTag.CHEESY), "치즈 맛있다", "패티도 좋다", null));
    mvc.perform(get("/v1/search").contextPath("/v1").param("keyword", "치즈"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("data.menus.content[0].name").value("합성 치즈버거"))
        .andExpect(jsonPath("data.reviews.content[0].title").value("치즈 맛있다"));
    mvc.perform(get("/v1/search").contextPath("/v1").param("keyword", " "))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/v1/search").contextPath("/v1").param("keyword", "치즈").param("limit", "101"))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/v1/menus").contextPath("/v1").param("brandId", "abc"))
        .andExpect(status().isBadRequest());
  }
}
