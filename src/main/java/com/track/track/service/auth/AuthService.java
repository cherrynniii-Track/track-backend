package com.track.track.service.auth;

import com.track.track.domain.Member;
import com.track.track.dto.auth.LoginRequest;
import com.track.track.dto.auth.LoginResponse;
import com.track.track.dto.auth.SignupRequest;
import com.track.track.dto.auth.SignupResponse;
import com.track.track.enums.Role;
import com.track.track.exception.BusinessException;
import com.track.track.exception.ErrorCode;
import com.track.track.jwt.JwtTokenProvider;
import com.track.track.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원가입 및 로그인 기능을 담당하는 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;


    /**
     * 회원가입 진행
     * @param request 회원가입 요청 정보
     * @return 회원가입 결과
     * @throws IllegalArgumentException 이미 사용 중인 이메일인 경우
     */
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        // 이메일 중복 검사
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 회원 엔티티 생성
        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .role(Role.ROLE_USER)
                .build();

        // 회원 저장
        Member savedMember = memberRepository.save(member);

        // 회원가입 결과 반환
        return new SignupResponse(
                savedMember.getId(),
                savedMember.getEmail(),
                savedMember.getNickname()
        );
    }

    /**
     * 로그인을 수행하고 JWT 발급
     * @param request 로그인 요청 정보
     * @return Access Token과 Refresh Token
     * @throws IllegalArgumentException 이메일 또는 비밀번호가 일치하지 않는 경우
     */
    public LoginResponse login(LoginRequest request) {
        // 이메일로 회원 조회
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }

        // 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(member.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail());

        // 로그인 결과 반환
        return new LoginResponse(accessToken, refreshToken);
    }
}