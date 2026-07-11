package com.track.track.controller.task;

import com.track.track.dto.task.TaskCreateRequest;
import com.track.track.dto.task.TaskResponse;
import com.track.track.dto.task.TaskUpdateRequest;
import com.track.track.service.auth.CustomUserDetails;
import com.track.track.service.task.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long projectId,
            @RequestBody TaskCreateRequest request
    ) {
        TaskResponse response = taskService.createTask(
                userDetails.getMemberId(),
                projectId,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(
                taskService.getTasks(userDetails.getMemberId(), projectId)
        );
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long projectId,
            @PathVariable Long taskId
    ) {
        return ResponseEntity.ok(
                taskService.getTask(
                        userDetails.getMemberId(),
                        projectId,
                        taskId
                )
        );
    }

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<List<TaskResponse>> getTasksByCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long projectId,
            @PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(
                taskService.getTasksByCategory(
                        userDetails.getMemberId(),
                        projectId,
                        categoryId
                )
        );
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<Void> updateTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestBody TaskUpdateRequest request
    ) {
        taskService.updateTask(
                userDetails.getMemberId(),
                projectId,
                taskId,
                request
        );

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long projectId,
            @PathVariable Long taskId
    ) {
        taskService.deleteTask(
                userDetails.getMemberId(),
                projectId,
                taskId
        );

        return ResponseEntity.noContent().build();
    }
}