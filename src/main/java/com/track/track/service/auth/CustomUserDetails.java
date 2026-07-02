package com.track.track.service.auth;

import com.track.track.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security에서 사용하는 사용자 정보(UserDetails) 구현체
 * Member 엔티티를 UserDetails 형태로 변환하여 인증 및 인가에 사용한다.
 */
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    // 로그인한 회원 정보
    private final Member member;

    /**
     * 사용자의 권한(Role) 반환
     * @return 사용자 권한 목록
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(member.getRole().name()));
    }

    /**
     * 사용자의 비밀번호를 반환한다.
     * @return 암호화된 비밀번호
     */
    @Override
    public String getPassword() {
        return member.getPassword();
    }

    /**
     * 사용자의 아이디(username)을 반환
     * @return 사용자 이메일
     */
    @Override
    public String getUsername() {
        return member.getEmail();
    }

    /**
     * 계정 만료 여부를 반환
     * @return 현재는 계정 만료 기능을 사용하지 않으므로 항상 true를 반환
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 계정 잠금 여부를 반환
     * @return 현재는 계정 잠금 기능을 사용하지 않으므로 항상 true를 반환
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 비밀번호 만료 여부를 반환
     * @return 현재는 비밀번호 만료 기능을 사용하지 않으므로 항상 true를 반환
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 계정 활성화 여부 반환
     * @return 현재는 계정 비활성화 기능을 사용하지 않으므로 항상 true를 반환
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * 회원 엔티티 반환
     * @return 회원 정보
     */
    public Member getMember() {
        return member;
    }

    /**
     * 회원 닉네임 반환
     * @return 닉네임
     */
    public String getNickname() {
        return member.getNickname();
    }

    /**
     * 회원 ID 반환
     * @return 회원 ID
     */
    public Long getMemberId() {
        return member.getId();
    }
}