package com.stocat.amumal.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// JWT에서 추출한 userId를 컨트롤러 파라미터에 주입받기 위한 어노테이션
// JwtAuthenticationFilter에서 request attribute에 userId 저장
// AuthUserIdArgumentResolver에서 @AuthUserId 파라미터에 꺼내서 주입
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthUserId {}
