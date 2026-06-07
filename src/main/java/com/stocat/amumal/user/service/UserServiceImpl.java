package com.stocat.amumal.user.service;

import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.user.domain.User;
import com.stocat.amumal.user.dto.SignUpRequest;
import com.stocat.amumal.user.dto.SignUpResponse;
import com.stocat.amumal.user.dto.UpdatePasswordRequest;
import com.stocat.amumal.user.dto.UpdateProfileRequest;
import com.stocat.amumal.user.dto.UpdateProfileResponse;
import com.stocat.amumal.user.dto.UserResponse;
import com.stocat.amumal.user.repository.UserRepository;
import com.stocat.amumal.user.validator.UserValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserValidator userValidator;

    public UserServiceImpl(UserRepository userRepository, UserValidator userValidator) {
        this.userRepository = userRepository;
        this.userValidator = userValidator;
    }

    @Override
    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        validateSignUp(request);

        if (userRepository.existsByEmail(request.email().trim())) {
            throw new ApiException(ErrorCode.DUPLICATE_EMAIL);
        }

        if (userRepository.existsByNickname(request.nickname().trim())) {
            throw new ApiException(ErrorCode.DUPLICATE_NICKNAME);
        }

        User savedUser = userRepository.save(User.of(
                request.email().trim(),
                request.password(),
                request.nickname().trim(),
                request.profileImage().trim()
        ));

        return new SignUpResponse(savedUser.getId());
    }

    @Override
    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl()
        );
    }

    @Override
    @Transactional
    public UpdateProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        validateProfileUpdate(request, user);

        String nickname = request.nickname() == null ? user.getNickname() : request.nickname().trim();
        String profileImageUrl =
                request.profileImage() == null ? user.getProfileImageUrl() : request.profileImage().trim();

        user.updateProfile(nickname, profileImageUrl);

        return new UpdateProfileResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImageUrl()
        );
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        validatePasswordUpdate(request);

        user.updatePassword(request.password());
    }

    @Override
    public void validateUserExists(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    private void validateSignUp(SignUpRequest request) {
        userValidator.validateEmail(request.email());
        userValidator.validatePassword(request.password());
        userValidator.validatePasswordConfirm(request.password(), request.passwordConfirm());
        userValidator.validateNickname(request.nickname());
        userValidator.validateRequiredProfileImage(request.profileImage());
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

    private void validatePasswordUpdate(UpdatePasswordRequest request) {
        userValidator.validatePassword(request.password());
        userValidator.validatePasswordUpdateConfirm(request.password(), request.passwordConfirm());
    }
}
