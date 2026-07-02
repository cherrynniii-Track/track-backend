package com.track.track.repository;

import com.track.track.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // 특정 프로젝트에 속한 카테고리 목록 조회
    List<Category> findByProjectId(Long projectId);
    
    // 같은 프로젝트 내에서 카테고리 이름 중복 확인
    boolean existsByProjectIdAndName(Long projectId, String name);
}
