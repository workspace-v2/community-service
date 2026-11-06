package com.stocat.amumal.post.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.stocat.amumal.menu.domain.Brand;
import com.stocat.amumal.menu.domain.Menu;
import com.stocat.amumal.menu.service.MenuService;
import com.stocat.amumal.post.domain.Post;
import com.stocat.amumal.post.domain.ReviewTag;
import com.stocat.amumal.post.dto.PostSearchSort;
import com.stocat.amumal.post.dto.PostSummaryResponse;
import com.stocat.amumal.user.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    properties = {
      "spring.profiles.include=",
      "spring.jpa.properties.hibernate.query.fail_on_pagination_over_collection_fetch=true"
    })
@ActiveProfiles("test")
@Transactional
class PostReviewQueryCountTest {
  @Autowired private PostService service;
  @Autowired private MenuService menus;
  @Autowired private EntityManager em;
  @Autowired private EntityManagerFactory emf;
  @MockitoBean private PostViewService postViewService;
  private Statistics statistics;
  private final List<Long> ids = new ArrayList<>();

  @BeforeEach
  void prepare() {
    for (int i = 0; i < 101; i++) {
      Brand brand = Brand.of("brand-" + i);
      em.persist(brand);
      Menu menu = Menu.of(brand, "menu-" + i, "description", 6000, Menu.SourceType.SYNTHETIC, null);
      em.persist(menu);
      User user = User.of("query-" + i + "@example.com", "Password1!", "writer" + i, null);
      em.persist(user);
      Post post =
          Post.of(
              user,
              menu,
              4,
              i % 2 == 0 ? Set.of(ReviewTag.SPICY, ReviewTag.JUICY) : Set.of(),
              "keyword " + i,
              "body",
              null);
      em.persist(post);
      ids.add(post.getId());
    }
    em.flush();
    em.clear();
    ids.sort(Comparator.reverseOrder());
    statistics = emf.unwrap(SessionFactory.class).getStatistics();
    statistics.clear();
  }

  @ParameterizedTest
  @CsvSource({
    "offset,1",
    "offset,10",
    "offset,100",
    "cursor,1",
    "cursor,10",
    "cursor,100",
    "recent,1",
    "recent,10",
    "recent,100",
    "relevance,1",
    "relevance,10",
    "relevance,100"
  })
  void pageUsesTwoSelectsWithDistinctMenusBrandsAndAuthors(String path, int limit) {
    List<PostSummaryResponse> rows;
    if (path.equals("offset")) {
      rows = service.getPostsByOffset(0, limit);
    } else if (path.equals("cursor")) {
      var slice = service.getPostsByCursor(null, null, limit);
      assertThat(slice.hasNext()).isTrue();
      assertThat(slice.nextCursor()).isEqualTo(ids.get(limit - 1));
      rows = slice.content();
    } else {
      var slice =
          service.searchPosts(
              null,
              "keyword",
              0,
              limit,
              path.equals("recent") ? PostSearchSort.RECENT : PostSearchSort.RELEVANCE);
      assertThat(slice.hasNext()).isTrue();
      rows = slice.content();
    }
    assertThat(rows)
        .extracting(PostSummaryResponse::id)
        .containsExactlyElementsOf(ids.subList(0, limit));
    for (var row : rows) {
      int index = Integer.parseInt(row.title().substring("keyword ".length()));
      assertThat(row.menuName()).isEqualTo("menu-" + index);
      assertThat(row.brandName()).isEqualTo("brand-" + index);
      assertThat(row.author().nickname()).isEqualTo("writer" + index);
      assertThat(row.tags())
          .isEqualTo(index % 2 == 0 ? Set.of(ReviewTag.SPICY, ReviewTag.JUICY) : Set.of());
    }
    assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);
  }

  @ParameterizedTest
  @CsvSource({"1,true", "10,true", "100,true", "1,false", "10,false", "100,false"})
  void menuPageFetchesBrandsInOneSelect(int limit, boolean relevance) {
    var page = menus.list("menu-", null, null, 0, limit, relevance);
    assertThat(page.content()).hasSize(limit);
    assertThat(page.hasNext()).isTrue();
    assertThat(page.content())
        .allSatisfy(
            row -> {
              int index = Integer.parseInt(row.name().substring("menu-".length()));
              assertThat(row.brandName()).isEqualTo("brand-" + index);
            });
    assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
  }

  @Test
  void emptyPageDoesNotFetchTags() {
    assertThat(service.searchPosts(null, "no-match", 0, 10, PostSearchSort.RECENT).content())
        .isEmpty();
    assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
  }

  @Test
  void tagSearchReturnsCompleteTagSetsAndStableOffset() {
    var slice = service.searchPosts(null, "매콤", 10, 10, PostSearchSort.RECENT);
    assertThat(slice.hasNext()).isTrue();
    assertThat(slice.content())
        .extracting(PostSummaryResponse::id)
        .containsExactlyElementsOf(
            ids.stream().filter(id -> ids.indexOf(id) % 2 == 0).skip(10).limit(10).toList());
    assertThat(slice.content())
        .allSatisfy(
            row ->
                assertThat(row.tags()).containsExactlyInAnyOrder(ReviewTag.SPICY, ReviewTag.JUICY));
    assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);
  }
}
