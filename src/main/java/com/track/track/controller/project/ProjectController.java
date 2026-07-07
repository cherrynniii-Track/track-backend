package com.track.track.controller.project;

import com.track.track.dto.project.ProjectCreateRequest;
import com.track.track.dto.project.ProjectResponse;
import com.track.track.dto.project.ProjectUpdateRequest;
import com.track.track.service.auth.CustomUserDetails;
import com.track.track.service.project.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ProjectCreateRequest request
    ) {
        ProjectResponse response = projectService.createProject(userDetails.getMemberId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getProjects(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(projectService.getProjects(userDetails.getMemberId()));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(projectService.getProject(userDetails.getMemberId(), projectId));
    }

    @PatchMapping("/{projectId}")
    public ResponseEntity<Void> updateProject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long projectId,
            @RequestBody ProjectUpdateRequest request
    ) {
        projectService.updateProject(userDetails.getMemberId(), projectId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long projectId
    ) {
        projectService.deleteProject(userDetails.getMemberId(), projectId);
        return ResponseEntity.noContent().build();
    }
}
