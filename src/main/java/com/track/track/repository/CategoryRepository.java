package com.track.track.repository;

import com.track.track.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // 특정 프로젝트에 속한 카테고리 목록 조회
    List<Category> findByProjectId(Long projectId);
    
    // 같은 프로젝트 내에서 카테고리 이름 중복 확인
    boolean existsByProjectIdAndName(Long projectId, String name);

    // 자기 자신을 제외하고 같은 이름이 있는지 검사
    boolean existsByProjectIdAndNameAndIdNot(Long projectId, String name, Long categoryId);

    // 프로젝트 ID와 카테고리 이름으로 찾기
    Optional<Category> findByProjectIdAndName(Long projectId, String name);
}
