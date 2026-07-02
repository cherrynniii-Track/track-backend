package com.track.track.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * JWT 생성 및 검증을 담당하는 클래스
 * Access Token과 Refresh Token 생성하고,
 * JWT에서 사용자 정보를 추출하거나 토큰의 유효성 검사
 */
@Component
public class JwtTokenProvider {

    // JWT 서명에 사용할 키
    @Value("${jwt.secret}")
    private String secretKey;

    // Access Token 만료 시간(ms)
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    // Refresh Token 만료 시간(ms)
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    // JWT 서명 및 검증에 사용할 Key 객체
    private Key key;

    /**
     * 애플리케이션 실행 시 Secret key를 Key 객체로 변환
     */
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Access Token 생성
     * @param email 사용자 이메일
     * @return 생성된 Access Token
     */
    public String createAccessToken(String email) {
        return createToken(email, accessTokenExpiration);
    }

    /**
     * Refresh Token 생성
     * @param email 사용자 이메일
     * @return 생성된 Refresh Token
     */
    public String createRefreshToken(String email) {
        return createToken(email, refreshTokenExpiration);
    }

    /**
     * JWT에서 사용자 이메일을 추출한다.
     * @param token JWT 문자열
     * @return 사용자 이메일
     */
    public String getEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * JWT의 유효성을 검사한다
     * 서명의 올바르고 만료되지 않은 경우 true를 반환한다.
     * @param token JWT 문자열
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * JWT를 생성한다.
     * @param email 사용자 이메일
     * @param expirationTime 토큰 만료 시간(ms)
     * @return 생성된 JWT
     */
    private String createToken(String email, long expirationTime) {
        Date now = new Date();      // 현재 시간
        Date expiration = new Date(now.getTime() + expirationTime); // 만료 시간 = 현재 시간 + 설정된 만료 시간

        return Jwts.builder()
                .subject(email)             // JWT subject에 이메일 저장
                .issuedAt(now)              // 토큰 발급 시간
                .expiration(expiration)     // 토큰 만료 시간
                .signWith(key)              // 비밀키로 서명
                .compact();                 // 최종 JWT 문자열 생성
    }

    /**
     * JWT를 파싱하여 Claims 반환
     * 서명 검증과 만료 시간 검증도 함께 수행된다.
     * @param token JWT 문자열
     * @return JWT에 저장된 Claims
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}