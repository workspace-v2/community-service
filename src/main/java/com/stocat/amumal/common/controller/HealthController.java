package com.stocat.amumal.common.controller;

import com.stocat.amumal.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

  @GetMapping("/health")
  public ApiResponse<String> health() {
    return ApiResponse.of("ok", "ok");
  }

  @GetMapping("/health/startup")
  public ApiResponse<String> startup() {
    return ApiResponse.of("ok", "ok");
  }

  @GetMapping("/health/readiness")
  public ApiResponse<String> readiness() {
    return ApiResponse.of("ok", "ok");
  }

  @GetMapping("/health/liveness")
  public ApiResponse<String> liveness() {
    return ApiResponse.of("ok", "ok");
  }
}
