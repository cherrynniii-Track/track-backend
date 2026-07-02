package com.track.track.jwt;

import com.track.track.service.auth.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 모든 요청마다 JWT를 검사하는 필터
 * 요청 헤더의 Authorization 값을 확인하여 JWT를 검증하고,
 * 유효한 토큰이면 Spring Security에 로그인 정보 저장
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    // 모든 요청마다 한 번씩 실행되는 메서드
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // Authorization 헤더에서 JWT 추출
        String token = resolveToken(request);

        // 토큰이 존재하고, 토큰이 유효하며, AccessToken이고, 아직 인증 정보가 없는 경우
        if (token != null
                && jwtTokenProvider.validateToken(token)
                && jwtTokenProvider.isAccessToken(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            // JWT 안에 저장된 이메일 추출 -> 사용자 정보 조회
            String email = jwtTokenProvider.getEmail(token);
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

            // Spring Security가 사용하는 인증 객체 생성
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,                    // 로그인한 사용자 정보
                            null,                           // 비밀번호는 이미 검증 되었으므로 null
                            userDetails.getAuthorities()    // 사용자의 권한(Role)
                    );

            // 요청 정보(IP, Session 등) 인증 객체에 저장
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            // 현재 요청을 로그인된 상태로 등록 (SecurityContext에 저장)
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    /**
     * Authorization 헤더에서 JWT만 추출
     * @param request 클라이언트의 HTTP 요청
     * @return Bearer 접두사를 제외한 JWT 문자열, 없거나 형식이 올바르지 않으면 {@code null}
     */
    private String resolveToken(HttpServletRequest request) {
        // Authorization 헤더 조회
        String bearerToken = request.getHeader("Authorization");

        // Bearer 로 시작하는 경우 JWT만 반환
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 토큰이 없거나 형식이 잘못된 경우
        return null;
    }
}