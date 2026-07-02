package com.track.track.repository;

import com.track.track.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // 이메일로 회원 조회
    Optional<Member> findByEmail(String email);
    
    // 해당 이메일의 회원이 존재하는지 확인
    boolean existsByEmail(String email);
}
