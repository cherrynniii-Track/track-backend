package com.track.track.repository;

import com.track.track.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    // 특정 회원의 프로젝트 목록 반환
    List<Project> findByMemberId(Long memberId);
    
    // 회원 ID와 프로젝트 이름으로 프로젝트 찾기
    Optional<Project> findByMemberIdAndName(Long memberId, String name);
}
