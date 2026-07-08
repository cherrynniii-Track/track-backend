package com.track.track.controller.category;

import com.track.track.dto.category.CategoryCreateRequest;
import com.track.track.dto.category.CategoryResponse;
import com.track.track.dto.category.CategoryUpdateRequest;
import com.track.track.service.auth.CustomUserDetails;
import com.track.track.service.category.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long projectId,
            @RequestBody @Valid CategoryCreateRequest request
    ) {
        CategoryResponse response = categoryService.createCategory(userDetails.getMemberId(), projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(categoryService.getCategories(userDetails.getMemberId(), projectId));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long projectId,
            @PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(categoryService.getCategory(userDetails.getMemberId(), projectId, categoryId));
    }

    @PatchMapping("/{categoryId}")
    public ResponseEntity<Void> updateCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long projectId,
            @PathVariable Long categoryId,
            @RequestBody @Valid CategoryUpdateRequest request
    ) {
        categoryService.updateCategory(userDetails.getMemberId(), projectId, categoryId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long projectId,
            @PathVariable Long categoryId
    ) {
        categoryService.deleteCategory(userDetails.getMemberId(), projectId, categoryId);
        return ResponseEntity.noContent().build();
    }
}