package com.stocat.amumal.user.service;

import com.stocat.amumal.user.dto.UpdatePasswordRequest;
import com.stocat.amumal.user.dto.UserResponse;

public interface UserService {

  UserResponse getUser(Long userId);

  void updatePassword(Long userId, UpdatePasswordRequest request);

  void validateUserExists(Long userId);

  void deleteUser(Long userId);

  void isEmailAvailable(String email);

  void isNicknameAvailable(String nickname);
}
