package com.track.track.service.support;

import com.track.track.domain.Category;
import com.track.track.exception.BusinessException;
import com.track.track.exception.ErrorCode;
import com.track.track.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CategorySupport {

    private final CategoryRepository categoryRepository;

    /**
     * 카테고리 목록 조회 및 프로젝트 소속 검증 후 카테고리 객체 리스트 반환
     */
    public List<Category> getValidatedCategoriesInProject(
            Long projectId,
            List<Long> categoryIds
    ) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }

        List<Long> distinctCategoryIds = categoryIds.stream()
                .distinct()
                .toList();

        List<Category> categories = categoryRepository.findAllById(distinctCategoryIds);

        if (categories.size() != distinctCategoryIds.size()) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        boolean hasCategoryFromAnotherProject = categories.stream()
                .anyMatch(category ->
                        !category.getProject().getId().equals(projectId)
                );

        if (hasCategoryFromAnotherProject) {
            throw new BusinessException(
                    ErrorCode.CATEGORY_PROJECT_MISMATCH
            );
        }

        return categories;
    }
}