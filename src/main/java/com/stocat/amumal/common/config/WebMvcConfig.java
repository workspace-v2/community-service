package com.stocat.amumal.common.config;

import com.stocat.amumal.auth.resolver.AuthUserIdArgumentResolver;
import com.stocat.amumal.image.config.ImageProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 커스텀 ArgumentResolver를 직접 만들어도 Spring이 자동으로 인식하지 못하므로, 등록
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthUserIdArgumentResolver authUserIdArgumentResolver;
    private final ImageProperties imageProperties;
    private final CorsProperties corsProperties;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authUserIdArgumentResolver);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ./uploads 같은 상대경로를 절대경로로 변환
        // "file:" 접두사는 Spring이 파일 시스템 경로임을 인식하는 데 필요
        String baseDir = imageProperties.getBaseDir();
        String resourceLocation = baseDir.startsWith("./")
                ? "file:" + System.getProperty("user.dir") + baseDir.substring(1) + "/"
                : "file:" + baseDir + "/";

        // GET /v1/images/** 요청을 업로드 디렉토리의 실제 파일로 매핑
        // ex) GET /v1/images/post-images/uuid.jpg → ./uploads/post-images/uuid.jpg 서빙
        registry.addResourceHandler("/images/**") // /images로 시작하는 모든 URL 요청을 가로챈다
                .addResourceLocations(resourceLocation); // 가로챈 요청을 resourceLocation에서 찾는다
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(corsProperties.getAllowedOrigins().toArray(String[]::new))
                .allowedMethods(
                        HttpMethod.GET.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.PATCH.name(),
                        HttpMethod.DELETE.name(),
                        HttpMethod.OPTIONS.name()
                )
                .allowCredentials(true)
                .allowedHeaders("*");
    }
}
