package com.stocat.amumal.common.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile("blue-green-lab")
public class BlueGreenLabConfig implements WebMvcConfigurer {

  private final String revision;
  private final boolean failBrands;
  private final Path failureFile;

  public BlueGreenLabConfig(
      @Value("${app.lab.revision}") String revision,
      @Value("${app.lab.brands.failure:false}") boolean failBrands,
      @Value("${app.lab.brands.failure-file:}") String failureFile) {
    this.revision = revision;
    this.failBrands = failBrands;
    this.failureFile = failureFile.isBlank() ? null : Path.of(failureFile);
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(
            new HandlerInterceptor() {
              @Override
              public boolean preHandle(
                  HttpServletRequest request, HttpServletResponse response, Object handler) {
                if (!HttpMethod.GET.matches(request.getMethod())) {
                  return true;
                }
                response.setHeader("X-App-Revision", revision);
                if (failBrands || (failureFile != null && Files.isRegularFile(failureFile))) {
                  response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                  return false;
                }
                return true;
              }
            })
        .addPathPatterns("/brands");
  }
}
