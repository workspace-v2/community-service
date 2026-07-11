package com.stocat.amumal.user.validator;

import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class UserValidator {

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
  private static final Pattern PASSWORD_PATTERN =
      Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,20}$");
  private static final Pattern IMAGE_FILE_PATTERN =
      Pattern.compile("^.+\\.(png|jpg|jpeg|gif|webp)$", Pattern.CASE_INSENSITIVE);

  public void validateEmail(String email) {
    if (isBlank(email)) {
      throw new ApiException(ErrorCode.EMPTY_EMAIL);
    }

    if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
      throw new ApiException(ErrorCode.INVALID_EMAIL_FORMAT);
    }
  }

  public void validatePassword(String password) {
    validateRequiredPassword(password);

    if (!PASSWORD_PATTERN.matcher(password).matches()) {
      throw new ApiException(ErrorCode.INVALID_PASSWORD_FORMAT);
    }
  }

  public void validateRequiredPassword(String password) {
    if (isBlank(password)) {
      throw new ApiException(ErrorCode.EMPTY_PASSWORD);
    }
  }

  public void validatePasswordConfirm(String password, String passwordConfirm) {
    if (isBlank(passwordConfirm)) {
      throw new ApiException(ErrorCode.EMPTY_PASSWORD_CONFIRM);
    }

    if (!password.equals(passwordConfirm)) {
      throw new ApiException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
    }
  }

  public void validatePasswordUpdateConfirm(String password, String passwordConfirm) {
    if (isBlank(passwordConfirm)) {
      throw new ApiException(ErrorCode.EMPTY_PASSWORD_CONFIRM);
    }

    if (!password.equals(passwordConfirm)) {
      throw new ApiException(ErrorCode.PASSWORD_UPDATE_CONFIRM_MISMATCH);
    }
  }

  public void validateNickname(String nickname) {
    if (isBlank(nickname)) {
      throw new ApiException(ErrorCode.EMPTY_NICKNAME);
    }

    if (nickname.chars().anyMatch(Character::isWhitespace)) {
      throw new ApiException(ErrorCode.NICKNAME_HAS_WHITESPACE);
    }

    if (nickname.trim().length() > 10) {
      throw new ApiException(ErrorCode.NICKNAME_TOO_LONG);
    }
  }

  public void validateRequiredProfileImage(String profileImage) {
    if (isBlank(profileImage)) {
      throw new ApiException(ErrorCode.EMPTY_PROFILE_IMAGE);
    }
  }

  public void validateOptionalProfileImage(String profileImage) {
    if (isBlank(profileImage) || !IMAGE_FILE_PATTERN.matcher(profileImage.trim()).matches()) {
      throw new ApiException(ErrorCode.INVALID_PROFILE_IMAGE);
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
