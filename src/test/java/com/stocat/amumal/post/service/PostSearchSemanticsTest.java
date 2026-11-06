package com.stocat.amumal.post.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.stocat.amumal.menu.domain.Brand;
import com.stocat.amumal.menu.domain.Menu;
import com.stocat.amumal.post.domain.Post;
import com.stocat.amumal.post.domain.ReviewTag;
import com.stocat.amumal.post.dto.PostSearchSort;
import com.stocat.amumal.post.dto.PostSummaryResponse;
import com.stocat.amumal.user.domain.User;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.profiles.include=")
@ActiveProfiles("test")
@Transactional
class PostSearchSemanticsTest {
  @Autowired private PostService service;
  @Autowired private EntityManager em;
  @MockitoBean private PostViewService postViewService;
  private final List<Row> rows = new ArrayList<>();
  private User author;
  private Menu classic;

  private record Row(
      Long id,
      Long menuId,
      String menu,
      String brand,
      String title,
      String body,
      Set<ReviewTag> tags,
      boolean eligible) {}

  @BeforeEach
  void prepare() {
    author = User.of("search-semantics@example.com", "Password1!", "searcher", null);
    em.persist(author);
    classic = menu("Classic", "Alpha");
    add(classic, "Needle both", "needle body", Set.of(), true);
    add(classic, "needle title", "plain body", Set.of(), true);
    add(classic, "plain title", "needle body", Set.of(), true);
    add(menu("Needle menu", "Beta"), "menu match", "plain", Set.of(), true);
    add(menu("Other", "Needle brand"), "brand match", "plain", Set.of(), true);
    add(classic, "tag match", "plain", Set.of(ReviewTag.SPICY, ReviewTag.JUICY), true);
    add(classic, "NEEDLE upper", "needle body", Set.of(ReviewTag.SPICY), true);
    add(classic, "literal %_!", "literal body", Set.of(), true);
    add(classic, "literal 1000", "plain", Set.of(), true);
    Menu deletedMenu = menu("needle deleted menu", "Gamma");
    deletedMenu.delete();
    add(deletedMenu, "old menu review", "plain", Set.of(), true);
    Menu deletedBrandMenu = menu("Other", "needle deleted brand");
    deletedBrandMenu.getBrand().delete();
    add(deletedBrandMenu, "old brand review", "plain", Set.of(), true);
    Post deleted = add(classic, "needle deleted", "needle", Set.of(), false);
    deleted.delete();
    Post legacy = add(classic, "needle legacy", "needle", Set.of(), false);
    em.flush();
    em.createNativeQuery("UPDATE post SET menu_id=NULL,rating=NULL WHERE post_id=:id")
        .setParameter("id", legacy.getId())
        .executeUpdate();
    em.clear();
  }

  @ParameterizedTest
  @CsvSource({
    "needle,RELEVANCE,0,3,false", "needle,RELEVANCE,1,3,false",
    "needle,RELEVANCE,5,3,false", "needle,RELEVANCE,100,3,false",
    "needle,RELEVANCE,0,100,false", "NEEDLE,RELEVANCE,0,100,false",
    "needle,RECENT,0,3,false", "needle,RECENT,1,3,false",
    "needle,RELEVANCE,0,3,true", "needle,RECENT,0,3,true",
    "매콤,RELEVANCE,0,3,false", "매콤,RECENT,0,3,false",
    "missing,RELEVANCE,0,3,false", "missing,RECENT,0,3,false",
    "%,RELEVANCE,0,3,false", "_,RELEVANCE,0,3,false",
    "!,RELEVANCE,0,3,false", "%,RECENT,0,3,false"
  })
  void preservesMatchingRankingFiltersAndPageBoundaries(
      String keyword, PostSearchSort sort, int offset, int limit, boolean filtered) {
    Long menuId = filtered ? classic.getId() : null;
    String needle = keyword.toLowerCase(Locale.ROOT);
    var matchingTags = ReviewTag.matching(keyword);
    Comparator<Row> order = Comparator.comparing(Row::id, Comparator.reverseOrder());
    if (sort == PostSearchSort.RELEVANCE) {
      order =
          Comparator.<Row, Boolean>comparing(r -> contains(r.title(), needle))
              .reversed()
              .thenComparing(
                  Comparator.<Row, Boolean>comparing(r -> contains(r.body(), needle)).reversed())
              .thenComparing(Row::id, Comparator.reverseOrder());
    }
    List<Row> expected =
        rows.stream()
            .filter(Row::eligible)
            .filter(r -> menuId == null || r.menuId().equals(menuId))
            .filter(
                r ->
                    contains(r.title(), needle)
                        || contains(r.body(), needle)
                        || contains(r.menu(), needle)
                        || contains(r.brand(), needle)
                        || r.tags().stream().anyMatch(matchingTags::contains))
            .sorted(order)
            .skip(offset)
            .toList();
    var result = service.searchPosts(menuId, keyword, offset, limit, sort);
    assertThat(result.content())
        .extracting(PostSummaryResponse::id)
        .containsExactlyElementsOf(expected.stream().limit(limit).map(Row::id).toList());
    assertThat(result.content()).extracting(PostSummaryResponse::id).doesNotHaveDuplicates();
    assertThat(result.hasNext()).isEqualTo(expected.size() > limit);
    assertThat(result.offset()).isEqualTo(offset);
    assertThat(result.limit()).isEqualTo(limit);
  }

  private boolean contains(String text, String keyword) {
    return text.toLowerCase(Locale.ROOT).contains(keyword);
  }

  private Menu menu(String name, String brandName) {
    Brand brand = Brand.of(brandName);
    em.persist(brand);
    Menu menu = Menu.of(brand, name, "description", 6000, Menu.SourceType.SYNTHETIC, null);
    em.persist(menu);
    return menu;
  }

  private Post add(Menu menu, String title, String body, Set<ReviewTag> tags, boolean eligible) {
    Post post = Post.of(author, menu, 4, tags, title, body, null);
    em.persist(post);
    rows.add(
        new Row(
            post.getId(),
            menu.getId(),
            menu.getName(),
            menu.getBrand().getName(),
            title,
            body,
            tags,
            eligible));
    return post;
  }
}
