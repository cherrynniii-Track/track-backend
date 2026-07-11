package com.track.track.dto.task;

import com.track.track.domain.Category;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaskCategoryResponse {

    private Long id;
    private String name;

    public static TaskCategoryResponse from(Category category) {
        return TaskCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}