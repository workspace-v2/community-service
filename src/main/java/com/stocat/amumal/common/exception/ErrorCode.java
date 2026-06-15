package com.stocat.amumal.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // Auth
    INVALID_TOKEN("INVALID_TOKEN", HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    // User
    USER_NOT_FOUND("USER_NOT_FOUND", HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    DUPLICATE_EMAIL("ALREADY_EXIST_EMAIL", HttpStatus.CONFLICT, "중복된 이메일 입니다."),
    DUPLICATE_NICKNAME("ALREADY_EXIST_NICKNAME", HttpStatus.CONFLICT, "중복된 닉네임 입니다."),
    INVALID_CREDENTIALS("INVALID_INPUT", HttpStatus.BAD_REQUEST, "아이디 또는 비밀번호를 확인해주세요."),
    MISSING_PROFILE_UPDATE_FIELD("INVALID_INPUT", HttpStatus.BAD_REQUEST, "닉네임 또는 프로필 이미지를 입력해주세요."),

    // User - validation
    EMPTY_EMAIL("INVALID_INPUT", HttpStatus.BAD_REQUEST, "이메일을 입력해주세요."),
    INVALID_EMAIL_FORMAT("INVALID_INPUT", HttpStatus.BAD_REQUEST, "올바른 이메일 주소 형식을 입력해주세요. (예: example@adapterz.kr)"),
    EMPTY_PASSWORD("INVALID_INPUT", HttpStatus.BAD_REQUEST, "비밀번호를 입력해주세요."),
    INVALID_PASSWORD_FORMAT("INVALID_INPUT", HttpStatus.BAD_REQUEST, "비밀번호는 8자 이상, 20자 이하이며, 대문자, 소문자, 숫자, 특수문자를 각각 최소 1개 포함해야 합니다."),
    EMPTY_PASSWORD_CONFIRM("INVALID_INPUT", HttpStatus.BAD_REQUEST, "비밀번호를 한번 더 입력해주세요."),
    PASSWORD_CONFIRM_MISMATCH("INVALID_INPUT", HttpStatus.BAD_REQUEST, "비밀번호가 다릅니다."),
    PASSWORD_UPDATE_CONFIRM_MISMATCH("INVALID_INPUT", HttpStatus.BAD_REQUEST, "비밀번호 확인과 다릅니다."),
    EMPTY_NICKNAME("INVALID_INPUT", HttpStatus.BAD_REQUEST, "닉네임을 입력해주세요."),
    NICKNAME_HAS_WHITESPACE("INVALID_INPUT", HttpStatus.BAD_REQUEST, "띄어쓰기를 없애주세요."),
    NICKNAME_TOO_LONG("INVALID_INPUT", HttpStatus.BAD_REQUEST, "닉네임은 최대 10자 까지 작성 가능합니다."),
    EMPTY_PROFILE_IMAGE("INVALID_INPUT", HttpStatus.BAD_REQUEST, "프로필 사진을 추가해주세요."),
    INVALID_PROFILE_IMAGE("INVALID_INPUT", HttpStatus.BAD_REQUEST, "유효한 파일이 아닙니다.(올바른 사진 확장자가 아닐 경우)"),

    // Post
    POST_NOT_FOUND("POST_NOT_FOUND", HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    POST_AUTHOR_NOT_FOUND("POST_AUTHOR_NOT_FOUND", HttpStatus.NOT_FOUND, "게시글 작성자를 찾을 수 없습니다."),
    POST_UPDATE_FORBIDDEN("POST_UPDATE_FORBIDDEN", HttpStatus.FORBIDDEN, "게시글 작성자만 수정할 수 있습니다."),
    POST_DELETE_FORBIDDEN("POST_DELETE_FORBIDDEN", HttpStatus.FORBIDDEN, "게시글 작성자만 삭제할 수 있습니다."),

    // Post - like
    POST_ALREADY_LIKED("POST_ALREADY_LIKED", HttpStatus.CONFLICT, "이미 좋아요한 게시글입니다."),
    POST_LIKE_NOT_FOUND("POST_ALREADY_UNLIKED", HttpStatus.CONFLICT, "좋아요하지 않은 게시글입니다."),

    // Post - validation
    INVALID_PAGE_SIZE("INVALID_INPUT", HttpStatus.BAD_REQUEST, "size는 1 이상이어야 합니다."),
    INVALID_POST_SEARCH_SORT("INVALID_INPUT", HttpStatus.BAD_REQUEST, "지원하지 않는 정렬 방식입니다."),
    MISSING_POST_FIELDS("INVALID_INPUT", HttpStatus.BAD_REQUEST, "제목, 내용을 모두 작성해주세요."),
    EMPTY_POST_TITLE("INVALID_INPUT", HttpStatus.BAD_REQUEST, "제목을 입력해주세요."),
    POST_TITLE_TOO_LONG("INVALID_INPUT", HttpStatus.BAD_REQUEST, "제목은 최대 26자까지 작성 가능합니다."),
    EMPTY_POST_CONTENT("INVALID_INPUT", HttpStatus.BAD_REQUEST, "내용을 입력해주세요."),
    TOO_MANY_IMAGES("INVALID_INPUT", HttpStatus.BAD_REQUEST, "이미지 파일은 1개만 업로드할 수 있습니다."),

    // Comment
    COMMENT_NOT_FOUND("COMMENT_NOT_FOUND", HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
    COMMENT_UPDATE_FORBIDDEN("COMMENT_UPDATE_FORBIDDEN", HttpStatus.FORBIDDEN, "댓글 작성자만 수정할 수 있습니다."),
    COMMENT_DELETE_FORBIDDEN("COMMENT_DELETE_FORBIDDEN", HttpStatus.FORBIDDEN, "댓글 작성자만 삭제할 수 있습니다."),

    // Comment - validation
    EMPTY_COMMENT_CONTENT("INVALID_INPUT", HttpStatus.BAD_REQUEST, "댓글을 입력해주세요."),
    COMMENT_CONTENT_TOO_LONG("INVALID_INPUT", HttpStatus.BAD_REQUEST, "댓글은 최대 1500자까지 작성 가능합니다."),
    INVALID_COMMENT_OFFSET("INVALID_INPUT", HttpStatus.BAD_REQUEST, "offset은 limit의 배수여야 합니다."),

    // Image
    EMPTY_FILE("INVALID_INPUT", HttpStatus.BAD_REQUEST, "파일을 선택해주세요."),
    INVALID_IMAGE_FORMAT("INVALID_INPUT", HttpStatus.BAD_REQUEST, "지원하지 않는 이미지 형식입니다. (jpeg, png, gif, webp만 허용)"),
    FILE_UPLOAD_FAILED("FILE_UPLOAD_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 중 오류가 발생했습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;

    ErrorCode(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
