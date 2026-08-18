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
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class JwtProvider {

  private final JwtProperties jwtProperties;
  private Key key;

  // @PostConstruct: 빈 생성 후 1회 실행: secret 문자열을 HMAC(비밀키를 이용한 해시 기반 인증 방식) 서명용 Key 객체로 변환
  @PostConstruct
  public void init() {
    if (!StringUtils.hasText(jwtProperties.getSecret())) {
      throw new IllegalStateException("jwt.secret must be configured");
    }
    this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
  }

  private String createToken(String tokenType, Long userId, long expSeconds) {
    Instant now = Instant.now();

    return Jwts.builder()
        .subject(String.valueOf(userId)) // sub: 토큰 주체 (userId)
        .claim("token_type", tokenType) // payload의 token_type claim에 access/refresh 종류를 기록
        .issuedAt(Date.from(now)) // iat: 발급 시각
        .expiration(Date.from(now.plusSeconds(expSeconds))) // exp: 만료 시각
        .signWith((SecretKey) key, Jwts.SIG.HS256) // HS256 알고리즘으로 서명
        .compact();
  }

  public String createAccessToken(Long userId) {
    return createToken("access", userId, jwtProperties.getAccessTokenExpSeconds());
  }

  public String createRefreshToken(Long userId) {
    return createToken("refresh", userId, jwtProperties.getRefreshTokenExpSeconds());
  }

  // 토큰 파싱: 서명 검증 + 만료 검증, 실패 시 예외 발생
  public Jws<Claims> parse(String token) {
    return Jwts.parser().verifyWith((SecretKey) key).build().parseSignedClaims(token);
  }

  // token_type이 Access인지 확인: 리프레시 토큰으로 API 호출하는 것을 방지
  public boolean isAccessToken(String token) {
    return "access".equals(getTokenType(token));
  }

  public boolean isRefreshToken(String token) {
    return "refresh".equals(getTokenType(token));
  }

  public Long getUserId(String token) {
    return Long.valueOf(parse(token).getPayload().getSubject());
  }

  private String getTokenType(String token) {
    return parse(token).getPayload().get("token_type", String.class);
  }

  public Long getAccessTokenValidityInMilliseconds() {
    return jwtProperties.getAccessTokenExpSeconds() * 1000;
  }
}
