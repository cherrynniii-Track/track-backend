package com.track.track.dto.project;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProjectCreateRequest {
    private String name;
    private String description;
}
