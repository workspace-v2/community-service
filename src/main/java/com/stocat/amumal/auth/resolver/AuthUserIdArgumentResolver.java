package com.stocat.amumal.auth.resolver;

import com.stocat.amumal.auth.annotation.AuthUserId;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

// @AuthUserId 어노테이션이 붙은 컨트롤러 파라미터를 처리하는 ArgumentResolver
@Component
public class AuthUserIdArgumentResolver implements HandlerMethodArgumentResolver {

    // 처리할 파라미터인지 판단: @AuthUserId + Long 타입
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthUserId.class)
                && parameter.getParameterType().equals(Long.class);
    }

    // request attribute에서 userId를 꺼내 반환
    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        return webRequest.getAttribute("userId", NativeWebRequest.SCOPE_REQUEST);
    }
}
