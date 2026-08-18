package com.stocat.amumal.user.service;

import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.user.domain.User;
import com.stocat.amumal.user.dto.UpdatePasswordRequest;
import com.stocat.amumal.user.dto.UserResponse;
import com.stocat.amumal.user.repository.UserRepository;
import com.stocat.amumal.user.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserValidator userValidator;
  private final PasswordEncoder passwordEncoder;

  @Override
  public UserResponse getUser(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

    return new UserResponse(
        user.getId(), user.getEmail(), user.getNickname(), user.getProfileImageUrl());
  }

  @Override
  @Transactional
  public void updatePassword(Long userId, UpdatePasswordRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

    validatePasswordUpdate(request);
    if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
      throw new ApiException(ErrorCode.CURRENT_PASSWORD_MISMATCH);
    }

    user.updatePassword(passwordEncoder.encode(request.password()));
  }

  @Override
  public void validateUserExists(Long userId) {
    userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
  }

  @Override
  @Transactional
  public void deleteUser(Long userId) {
    userRepository.deleteById(userId);
  }

  @Override
  public void isEmailAvailable(String email) {
    userValidator.validateEmail(email);
    if (userRepository.existsByEmail(email)) {
      throw new ApiException(ErrorCode.DUPLICATE_EMAIL);
    }
  }

  @Override
  public void isNicknameAvailable(String nickname) {
    userValidator.validateNickname(nickname);
    if (userRepository.existsByNickname(nickname)) {
      throw new ApiException(ErrorCode.DUPLICATE_NICKNAME);
    }
  }

  private void validatePasswordUpdate(UpdatePasswordRequest request) {
    userValidator.validateRequiredPassword(request.currentPassword());
    userValidator.validatePassword(request.password());
    if (request.currentPassword().equals(request.password())) {
      throw new ApiException(ErrorCode.NEW_PASSWORD_SAME_AS_CURRENT);
    }
    if (request.passwordConfirm() != null && !request.passwordConfirm().isBlank()) {
      userValidator.validatePasswordUpdateConfirm(request.password(), request.passwordConfirm());
    }
  }
}
