package com.track.track.repository;

import com.track.track.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    // 특정 회원의 프로젝트 목록 반환
    List<Project> findByMemberId(Long memberId);

    // 특정 멤버가 특정 이름의 프로젝트를 갖는지 확인 (중복 방지)
    boolean existsByMemberIdAndName(Long memberId, String name);
}
