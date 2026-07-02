package com.track.track.service.auth;

import com.track.track.domain.Member;
import com.track.track.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security에서 사용자 정보 조회하는 서비스
 * 이메일 기준으로 회원을 조회하여 UserDetails 객체 반환
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    /**
     * 이메일을 이용하여 사용자 정보를 조회
     * @param email 로그인에 사용할 사용자 이메일
     * @return Spring Security에서 사용할 UserDetails 객체
     * @throws UsernameNotFoundException 존재하지 않는 회원일 경우
     */
    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        // 이메일로 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("존재하지 않는 회원입니다."));

        // Member을 UserDetails 형태로 반환
        return new CustomUserDetails(member);
    }
}