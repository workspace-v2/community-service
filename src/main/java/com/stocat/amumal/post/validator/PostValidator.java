package com.stocat.amumal.post.validator;

import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.post.dto.CreatePostRequest;
import com.stocat.amumal.post.dto.UpdatePostRequest;
import org.springframework.stereotype.Component;

@Component
public class PostValidator {

  public void validateCreatePost(CreatePostRequest request) {
    validateTitleAndContent(request.title(), request.content());
  }

  public void validateListSize(int size) {
    if (size <= 0) {
      throw new ApiException(ErrorCode.INVALID_PAGE_SIZE);
    }
  }

  public void validateUpdatePost(UpdatePostRequest request) {
    validateTitle(request.title());
    validateContent(request.content());
    validateSingleImage(request.image());
  }

  private void validateTitleAndContent(String title, String content) {
    if (isBlank(title) || isBlank(content)) {
      throw new ApiException(ErrorCode.MISSING_POST_FIELDS);
    }

    validateTitleLength(title);
  }

  private void validateTitle(String title) {
    if (isBlank(title)) {
      throw new ApiException(ErrorCode.EMPTY_POST_TITLE);
    }

    validateTitleLength(title);
  }

  private void validateTitleLength(String title) {
    if (title.trim().length() > 26) {
      throw new ApiException(ErrorCode.POST_TITLE_TOO_LONG);
    }
  }

  private void validateContent(String content) {
    if (isBlank(content)) {
      throw new ApiException(ErrorCode.EMPTY_POST_CONTENT);
    }
  }

  private void validateSingleImage(String image) {
    if (image != null && image.contains(",")) {
      throw new ApiException(ErrorCode.TOO_MANY_IMAGES);
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
