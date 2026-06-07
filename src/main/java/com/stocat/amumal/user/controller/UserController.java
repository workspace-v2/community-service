package com.stocat.amumal.user.controller;

import com.stocat.amumal.common.response.ApiResponse;
import com.stocat.amumal.user.dto.LoginRequest;
import com.stocat.amumal.user.dto.LoginResponse;
import com.stocat.amumal.user.dto.SignUpRequest;
import com.stocat.amumal.user.dto.SignUpResponse;
import com.stocat.amumal.user.dto.UpdatePasswordRequest;
import com.stocat.amumal.user.dto.UpdateProfileRequest;
import com.stocat.amumal.user.dto.UpdateProfileResponse;
import com.stocat.amumal.user.dto.UserResponse;
import com.stocat.amumal.user.service.UserService;
import com.stocat.amumal.user.usecase.DeleteUserUseCase;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final DeleteUserUseCase deleteUserUseCase;

    public UserController(UserService userService, DeleteUserUseCase deleteUserUseCase) {
        this.userService = userService;
        this.deleteUserUseCase = deleteUserUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SignUpResponse> signUp(@RequestBody SignUpRequest request) {
        return ApiResponse.of("회원가입이 완료되었습니다.", userService.signUp(request));
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.of("로그인이 완료되었습니다.", userService.login(request));
    }

    @GetMapping("/{user_id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserResponse> getUser(@Positive @PathVariable("user_id") Long userId) {
        return ApiResponse.of("회원정보 조회에 성공했습니다.", userService.getUser(userId));
    }

    @PatchMapping("/{user_id}/profile")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UpdateProfileResponse> updateProfile(
            @Positive @PathVariable("user_id") Long userId,
            @RequestBody UpdateProfileRequest request
    ) {
        return ApiResponse.of("수정 완료", userService.updateProfile(userId, request));
    }

    @PutMapping("/{user_id}/password")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> updatePassword(
            @Positive @PathVariable("user_id") Long userId,
            @RequestBody UpdatePasswordRequest request
    ) {
        userService.updatePassword(userId, request);
        return ApiResponse.of("수정 완료", null);
    }

    @DeleteMapping("/{user_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@Positive @PathVariable("user_id") Long userId) {
        deleteUserUseCase.execute(userId);
    }
}
