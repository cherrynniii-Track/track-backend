package com.track.track.service.category;

import com.track.track.domain.Category;
import com.track.track.domain.Project;
import com.track.track.dto.category.CategoryCreateRequest;
import com.track.track.dto.category.CategoryResponse;
import com.track.track.dto.category.CategoryUpdateRequest;
import com.track.track.exception.BusinessException;
import com.track.track.exception.ErrorCode;
import com.track.track.repository.CategoryRepository;
import com.track.track.service.support.ProjectSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProjectSupport projectSupport;

    /**
     * 카테고리 생성
     * @param memberId 회원
     * @param projectId 프로젝트
     * @param request 카테고리 생성 요청
     * @return 카테고리 생성 응답
     */
    @Transactional
    public CategoryResponse createCategory(Long memberId, Long projectId, CategoryCreateRequest request) {
        Project project = projectSupport.getProjectById(projectId);
        projectSupport.validateOwner(memberId, project);

        validateDuplicateName(projectId, request.getName());

        Category category = Category.builder()
                .project(project)
                .name(request.getName())
                .build();

        categoryRepository.save(category);

        return new CategoryResponse(category);
    }

    /**
     * 특정 프로젝트의 카테고리 목록을 반환
     * @param memberId 회원
     * @param projectId 프로젝트
     * @return 카테고리 목록
     */
    public List<CategoryResponse> getCategories(Long memberId, Long projectId) {
        Project project = projectSupport.getProjectById(projectId);
        projectSupport.validateOwner(memberId, project);

        return categoryRepository.findByProjectId(projectId)
                .stream()
                .map(CategoryResponse::new)
                .toList();
    }

    /**
     * 특정 카테고리 하나의 응답을 받아오기
     * @param memberId 회원
     * @param projectId 프로젝트
     * @param categoryId 카테고리
     * @return 카테고리 응답
     */
    public CategoryResponse getCategory(Long memberId, Long projectId, Long categoryId) {
        Project project = projectSupport.getProjectById(projectId);
        projectSupport.validateOwner(memberId, project);

        Category category = getCategoryById(categoryId);
        validateCategoryInProject(projectId, category);

        return new CategoryResponse(category);
    }

    /**
     * 카테고리 수정
     * @param memberId 회원
     * @param projectId 프로젝트
     * @param categoryId 카테고리
     * @param request 카테고리 수정 요청
     */
    @Transactional
    public void updateCategory(Long memberId, Long projectId, Long categoryId, CategoryUpdateRequest request) {
        Project project = projectSupport.getProjectById(projectId);
        projectSupport.validateOwner(memberId, project);

        Category category = getCategoryById(categoryId);
        validateCategoryInProject(projectId, category);

        if (request.getName() != null) {
            validateDuplicateName(projectId, request.getName());
        }

        category.update(request.getName());
    }

    /**
     * 카테고리 삭제
     * @param memberId 회원
     * @param projectId 프로젝트
     * @param categoryId 삭제할 카테고리
     */
    @Transactional
    public void deleteCategory(Long memberId, Long projectId, Long categoryId) {
        Project project = projectSupport.getProjectById(projectId);
        projectSupport.validateOwner(memberId, project);

        Category category = getCategoryById(categoryId);
        validateCategoryInProject(projectId, category);

        categoryRepository.delete(category);
    }

    /**
     * 카테고리 ID로 카테고리 받아오기
     * @param categoryId 카테고리 ID
     * @return 카테고리 객체
     */
    private Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    /**
     * 카테고리가 해당 프로젝트에 속한 카테고리인지 검증
     * @param projectId 검증할 프로젝트
     * @param category 검증할 카테고리
     */
    private void validateCategoryInProject(Long projectId, Category category) {
        if (!category.getProject().getId().equals(projectId)) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
    }

    /**
     * 중복된 카테고리 이름이 있는지 검사
     * @param projectId 프로젝트
     * @param name 카테고리 이름
     */
    private void validateDuplicateName(Long projectId, String name) {
        if (categoryRepository.existsByProjectIdAndName(projectId, name)) {
            throw new BusinessException(ErrorCode.CATEGORY_DUPLICATED);
        }
    }
}