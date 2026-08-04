package com.stocat.amumal.post.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.stocat.amumal.post.domain.Post;
import com.stocat.amumal.post.repository.PostRepository;
import com.stocat.amumal.user.domain.User;
import com.stocat.amumal.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
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

    @Autowired
    private PostService postService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    @DisplayName("게시글 목록 조회는 작성자 정보를 조회한다")
    void getPostsFetchesAuthorsInSingleQuery() {
        for (int i = 1; i <= 10; i++) {
            User user =
                    userRepository.save(
                            User.of(
                                    "nplus1-user-" + i + "@stocat.com",
                                    "Password1!",
                                    "writer" + i,
                                    "https://example.com/profile-" + i));
            postRepository.save(Post.of(user, "title-" + i, "content-" + i, null));
        }

        entityManager.flush();
        entityManager.clear();

        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        postService.getPosts(0, 10);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("게시글 검색 조회 시 작성자 정보를 함께 조회한다")
    void searchPostsFetchesAuthorsInSingleQuery() {
        User user =
                userRepository.save(
                        User.of("search-writer@stocat.com", "Password1!", "searcher", "https://example.com"));
        for (int i = 1; i <= 10; i++) {
            postRepository.save(Post.of(user, "keyword title " + i, "keyword content " + i, null));
        }

        entityManager.flush();
        entityManager.clear();

        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        postService.searchPosts("keyword", 0, 10, com.stocat.amumal.post.dto.PostSearchSort.RECENT);

        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1L);
    }
}
