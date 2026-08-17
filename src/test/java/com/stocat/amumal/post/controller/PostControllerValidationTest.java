package com.stocat.amumal.post.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.stocat.amumal.post.service.PostService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.lang.reflect.Method;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "spring.profiles.include=")
@ActiveProfiles("test")
class PostControllerValidationTest {

  @Autowired private Validator validator;

  @Test
  @DisplayName("커서 게시글 목록 조회에서 symbol 형식이 잘못되면 제약 조건 위반이 발생한다")
  void getPostsByCursorRejectsInvalidSymbolFormat() throws Exception {
    PostController controller = newController();
    Method method =
        PostController.class.getMethod("getPostsByCursor", String.class, Long.class, int.class);

    Set<ConstraintViolation<PostController>> violations =
        validator
            .forExecutables()
            .validateParameters(controller, method, new Object[] {"abc", null, 10});

    assertThat(violations).isNotEmpty();
  }

  @Test
  @DisplayName("게시글 검색에서 symbol 형식이 잘못되면 제약 조건 위반이 발생한다")
  void searchPostsRejectsInvalidSymbolFormat() throws Exception {
    PostController controller = newController();
    Method method =
        PostController.class.getMethod(
            "searchPosts", String.class, String.class, int.class, int.class, String.class);

    Set<ConstraintViolation<PostController>> violations =
        validator
            .forExecutables()
            .validateParameters(
                controller, method, new Object[] {"12345", "test", 0, 10, "recent"});

    assertThat(violations).isNotEmpty();
  }

  private PostController newController() {
    return new PostController(null, stubPostService(), null, null, null);
  }

  private PostService stubPostService() {
    return new PostService() {
      @Override
      public java.util.List<com.stocat.amumal.post.dto.PostSummaryResponse> getPostsByOffset(
          int offset, int limit) {
        throw new UnsupportedOperationException();
      }

      @Override
      public com.stocat.amumal.post.dto.PostCursorSliceResponse getPostsByCursor(
          String symbol, Long cursor, int limit) {
        throw new UnsupportedOperationException();
      }

      @Override
      public java.util.List<com.stocat.amumal.post.dto.PostSummaryResponse> searchPosts(
          String symbol,
          String keyword,
          int offset,
          int limit,
          com.stocat.amumal.post.dto.PostSearchSort sort) {
        throw new UnsupportedOperationException();
      }

      @Override
      public com.stocat.amumal.post.dto.GetPostResponse getPost(Long postId, Long userId) {
        throw new UnsupportedOperationException();
      }

      @Override
      public void deletePost(Long postId, Long userId) {
        throw new UnsupportedOperationException();
      }
    };
  }
}
