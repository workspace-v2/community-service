package com.stocat.amumal.user.service;

import com.stocat.amumal.user.dto.SignUpRequest;
import com.stocat.amumal.user.dto.SignUpResponse;
import com.stocat.amumal.user.dto.UpdatePasswordRequest;
import com.stocat.amumal.user.dto.UpdateProfileRequest;
import com.stocat.amumal.user.dto.UpdateProfileResponse;
import com.stocat.amumal.user.dto.UserResponse;

public interface UserService {

    SignUpResponse signUp(SignUpRequest request);

    UserResponse getUser(Long userId);

    UpdateProfileResponse updateProfile(Long userId, UpdateProfileRequest request);

    void updatePassword(Long userId, UpdatePasswordRequest request);

    void validateUserExists(Long userId);

    void deleteUser(Long userId);

    void isEmailAvailable(String email);

    void isNicknameAvailable(String nickname);
}
