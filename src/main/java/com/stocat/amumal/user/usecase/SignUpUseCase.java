package com.stocat.amumal.user.usecase;

import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.user.domain.User;
import com.stocat.amumal.user.dto.SignUpRequest;
import com.stocat.amumal.user.dto.SignUpResponse;
import com.stocat.amumal.user.repository.UserRepository;
import com.stocat.amumal.user.service.UserImageMappingService;
import com.stocat.amumal.user.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SignUpUseCase {

    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final UserImageMappingService userImageMappingService;

    @Transactional
    public SignUpResponse execute(SignUpRequest request) {
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
                request.profileImage() == null ? null : request.profileImage().trim()
        ));

        userImageMappingService.replace(savedUser, request.profileImage());

        return new SignUpResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getNickname(),
                savedUser.getProfileImageUrl()
        );
    }

    private void validateSignUp(SignUpRequest request) {
        userValidator.validateEmail(request.email());
        userValidator.validatePassword(request.password());
        userValidator.validateNickname(request.nickname());
        if (request.passwordConfirm() != null && !request.passwordConfirm().isBlank()) {
            userValidator.validatePasswordConfirm(request.password(), request.passwordConfirm());
        }
        if (request.profileImage() != null) {
            userValidator.validateOptionalProfileImage(request.profileImage());
        }
    }
}
