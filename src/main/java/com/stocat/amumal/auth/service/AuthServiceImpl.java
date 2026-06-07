package com.stocat.amumal.auth.service;

import com.stocat.amumal.auth.dto.LoginRequest;
import com.stocat.amumal.auth.dto.LoginResponse;
import com.stocat.amumal.common.exception.ApiException;
import com.stocat.amumal.common.exception.ErrorCode;
import com.stocat.amumal.user.domain.User;
import com.stocat.amumal.user.repository.UserRepository;
import com.stocat.amumal.user.validator.UserValidator;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserValidator userValidator;

    public AuthServiceImpl(UserRepository userRepository, UserValidator userValidator) {
        this.userRepository = userRepository;
        this.userValidator = userValidator;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        userValidator.validateEmail(request.email());
        userValidator.validatePassword(request.password());

        User user = userRepository.findByEmail(request.email().trim())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_CREDENTIALS));

        if (!user.getPassword().equals(request.password())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }

        return new LoginResponse(user.getId());
    }
}
