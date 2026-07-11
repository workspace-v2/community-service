package com.stocat.amumal.user.usecase;

import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.user.domain.User;
import com.stocat.amumal.user.dto.UpdateProfileRequest;
import com.stocat.amumal.user.dto.UpdateProfileResponse;
import com.stocat.amumal.user.repository.UserRepository;
import com.stocat.amumal.user.service.UserImageMappingService;
import com.stocat.amumal.user.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateProfileUseCase {

  private final UserRepository userRepository;
  private final UserValidator userValidator;
  private final UserImageMappingService userImageMappingService;

  @Transactional
  public UpdateProfileResponse execute(Long userId, UpdateProfileRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

    validateProfileUpdate(request, user);

    String nickname = request.nickname() == null ? user.getNickname() : request.nickname().trim();
    String profileImageUrl = request.profileImage() == null ? null : request.profileImage().trim();

    user.updateProfile(nickname, profileImageUrl);

    userImageMappingService.replace(user, request.profileImage());

    return new UpdateProfileResponse(user.getId(), user.getNickname(), user.getProfileImageUrl());
  }

  private void validateProfileUpdate(UpdateProfileRequest request, User user) {
    boolean hasNickname = request.nickname() != null;
    boolean hasProfileImage = request.profileImage() != null;

    if (!hasNickname && !hasProfileImage) {
      throw new ApiException(ErrorCode.MISSING_PROFILE_UPDATE_FIELD);
    }

    if (hasNickname) {
      String nickname = request.nickname().trim();
      userValidator.validateNickname(request.nickname());

      if (!nickname.equals(user.getNickname()) && userRepository.existsByNickname(nickname)) {
        throw new ApiException(ErrorCode.DUPLICATE_NICKNAME);
      }
    }

    if (hasProfileImage) {
      userValidator.validateOptionalProfileImage(request.profileImage());
    }
  }
}
