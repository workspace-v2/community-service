package com.stocat.amumal.user.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.user.domain.User;
import com.stocat.amumal.user.dto.UpdatePasswordRequest;
import com.stocat.amumal.user.repository.UserRepository;
import com.stocat.amumal.user.validator.UserValidator;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserServiceImplTest {

  private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
  private final UserValidator userValidator = new UserValidator();
  private final PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);

  private final UserServiceImpl userService =
      new UserServiceImpl(userRepository, userValidator, passwordEncoder);

  @Test
  void 새_비밀번호가_현재_비밀번호와_같으면_예외를_던진다() {
    User user = User.of("tester@stocat.com", "encodedPassword", "tester", null);
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    UpdatePasswordRequest request =
        new UpdatePasswordRequest("Password1!", "Password1!", "Password1!");

    assertThatThrownBy(() -> userService.updatePassword(1L, request))
        .isInstanceOf(ApiException.class)
        .extracting(exception -> ((ApiException) exception).getErrorCode())
        .isEqualTo(ErrorCode.NEW_PASSWORD_SAME_AS_CURRENT);
    verify(passwordEncoder, never()).matches(request.currentPassword(), user.getPassword());
    verify(passwordEncoder, never()).encode(request.password());
  }
}
