package com.stocat.amumal.user.controller;

import com.stocat.amumal.auth.annotation.AuthUserId;
import com.stocat.amumal.common.response.ApiResponse;
import com.stocat.amumal.image.dto.ProfileImageUploadResponse;
import com.stocat.amumal.user.dto.UpdatePasswordRequest;
import com.stocat.amumal.user.dto.UpdateProfileRequest;
import com.stocat.amumal.user.dto.UpdateProfileResponse;
import com.stocat.amumal.user.dto.UserResponse;
import com.stocat.amumal.user.service.UserService;
import com.stocat.amumal.user.usecase.DeleteUserUseCase;
import com.stocat.amumal.user.usecase.UpdateProfileUseCase;
import com.stocat.amumal.user.usecase.UploadProfileImageUseCase;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@AllArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UploadProfileImageUseCase uploadProfileImageUseCase;
    private final UserService userService;
    private final DeleteUserUseCase deleteUserUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserResponse> getUser(@AuthUserId Long userId) {
        return ApiResponse.of("회원정보 조회에 성공했습니다.", userService.getUser(userId));
    }

    @PutMapping("/me")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UpdateProfileResponse> updateProfile(
            @AuthUserId Long userId,
            @RequestBody UpdateProfileRequest request
    ) {
        return ApiResponse.of("수정 완료", updateProfileUseCase.execute(userId, request));
    }

    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> updatePassword(
            @AuthUserId Long userId,
            @RequestBody UpdatePasswordRequest request
    ) {
        userService.updatePassword(userId, request);
        return ApiResponse.of("수정 완료", null);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deleteUser(@AuthUserId Long userId) {
        deleteUserUseCase.execute(userId);
        return ApiResponse.of("회원 탈퇴가 완료되었습니다.", null);
    }

    @PostMapping("/upload/profile-image")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProfileImageUploadResponse> uploadProfileImage(
            @RequestParam("profileImage") MultipartFile file
    ) {
        return ApiResponse.of("프로필 이미지가 업로드되었습니다.", uploadProfileImageUseCase.execute(file));
    }

    @GetMapping("/email/check")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> checkEmail(@RequestParam("email") String email) {
        userService.isEmailAvailable(email);
        return ApiResponse.of("사용 가능한 이메일입니다.", null);
    }

    @GetMapping("/nickname/check")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> checkNickname(@RequestParam("nickname") String nickname) {
        userService.isNicknameAvailable(nickname);
        return ApiResponse.of("사용 가능한 닉네임입니다.", null);
    }
}
