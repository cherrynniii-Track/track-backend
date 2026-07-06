package com.track.track.dto.project;

import com.track.track.domain.Project;
import lombok.Getter;

@Getter
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;

    public ProjectResponse(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.description = project.getDescription();
    }
}
