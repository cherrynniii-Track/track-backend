package com.track.track.dto.category;

import com.track.track.domain.Category;
import lombok.Getter;

@Getter
public class CategoryResponse {

    private Long id;
    private String name;

    public CategoryResponse(Category category) {
        this.id = category.getId();
        this.name = category.getName();
    }
}
