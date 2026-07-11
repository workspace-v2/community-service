package com.stocat.amumal.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtProvider {

  private final JwtProperties jwtProperties;
  private Key key;

  // @PostConstruct: 빈 생성 후 1회 실행: secret 문자열을 HMAC(비밀키를 이용한 해시 기반 인증 방식) 서명용 Key 객체로 변환
  @PostConstruct
  public void init() {
    this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
  }

  // 토큰 생성 공통 로직
  private String createToken(
      String type, Long userId, Map<String, Object> claims, long expSeconds) {
    Instant now = Instant.now();

    return Jwts.builder()
        .subject(String.valueOf(userId)) // sub: 토큰 주체 (userId)
        .claim("typ", type) // typ: 토큰 타입 구분용 커스텀 클레임
        .claims(claims) // 추가 클레임 (email, nickname 등)
        .issuedAt(Date.from(now)) // iat: 발급 시각
        .expiration(Date.from(now.plusSeconds(expSeconds))) // exp: 만료 시각
        .signWith((SecretKey) key, Jwts.SIG.HS256) // HS256 알고리즘으로 서명
        .compact();
  }

  // 액세스 토큰: 짧은 만료, email/nickname 포함
  public String createAccessToken(Long userId, String email, String nickname) {
    return createToken(
        "access",
        userId,
        Map.of("email", email, "nickname", nickname),
        jwtProperties.getAccessTokenExpSeconds());
  }

  // 리프레시 토큰: 긴 만료 시간, userId만 포함
  public String createRefreshToken(Long userId) {
    return createToken("refresh", userId, Map.of(), jwtProperties.getRefreshTokenExpSeconds());
  }

  // 토큰 파싱: 서명 검증 + 만료 검증, 실패 시 예외 발생
  public Jws<Claims> parse(String token) {
    return Jwts.parser().verifyWith((SecretKey) key).build().parseSignedClaims(token);
  }

  // typ 클레임이 "access"인지 확인: 리프레시 토큰으로 API 호출하는 것을 방지
  public boolean isAccessToken(String token) {
    return "access".equals(parse(token).getPayload().get("typ", String.class));
  }

  // 토큰 subject(userId) 추출
  public Long getUserId(String token) {
    return Long.valueOf(parse(token).getPayload().getSubject());
  }

  // 액세스 토큰 만료 시간(ms)
  public Long getAccessTokenValidityInMilliseconds() {
    return jwtProperties.getAccessTokenExpSeconds() * 1000;
  }
}
