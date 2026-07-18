package com.track.track.service.task;

import com.track.track.domain.Category;
import com.track.track.domain.Project;
import com.track.track.domain.Task;
import com.track.track.dto.common.PageResponse;
import com.track.track.dto.task.TaskCreateRequest;
import com.track.track.dto.task.TaskResponse;
import com.track.track.dto.task.TaskSearchCondition;
import com.track.track.dto.task.TaskUpdateRequest;
import com.track.track.exception.BusinessException;
import com.track.track.exception.ErrorCode;
import com.track.track.repository.TaskRepository;
import com.track.track.service.support.CategorySupport;
import com.track.track.service.support.ProjectSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final CategorySupport categorySupport;
    private final ProjectSupport projectSupport;

    private static final int MAX_PAGE_SIZE = 100;

    /**
     * 작업 생성
     * @param memberId 작업을 생성하는 회원
     * @param projectId 작업이 속하는 프로젝트
     * @param request 작업 생성 요청 DTO
     * @return 생성된 작업
     */
    @CacheEvict(
            value = "dashboard",
            key = "#projectId"
    )
    @Transactional
    public TaskResponse createTask(
            Long memberId,
            Long projectId,
            TaskCreateRequest request
    ) {

        Project project = projectSupport.getProjectById(projectId);
        projectSupport.validateOwner(memberId, project);

        List<Category> categories = categorySupport.getValidatedCategoriesInProject(
                projectId,
                request.getCategoryIds()
        );

        Task task = Task.builder()
                .project(project)
                .title(request.getTitle())
                .goal(request.getGoal())
                .workProcess(request.getWorkProcess())
                .lessonLearned(request.getLessonLearned())
                .startedAt(request.getStartedAt())
                .finishedAt(request.getFinishedAt())
                .dueDate(request.getDueDate())
                .status(request.getStatus())
                .difficulty(request.getDifficulty())
                .priority(request.getPriority())
                .build();

        taskRepository.save(task);

        for (Category category : categories) {
            category.addTask(task);
        }

        return new TaskResponse(task);
    }

    /**
     * 프로젝트의 작업 목록 조회 (마감일 오름차순)
     * @param memberId 조회하는 회원
     * @param projectId 작업이 속한 프로젝트
     * @param condition 검색 조건
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 작업 목록
     */
    public PageResponse<TaskResponse> getTasks(
            Long memberId,
            Long projectId,
            TaskSearchCondition condition,
            int page,
            int size
    ) {
        Project project = projectSupport.getProjectById(projectId);
        projectSupport.validateOwner(memberId, project);

        validatePageRequest(page, size);
        validateSearchCondition(condition);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.asc("dueDate"),
                        Sort.Order.asc("id")
                )
        );

        Page<Task> tasks;

        if (condition.getCategoryId() == null) {
            tasks = taskRepository.findTasks(
                    projectId,
                    condition.getStatus(),
                    condition.getDifficulty(),
                    condition.getPriority(),
                    condition.getDueDateFrom(),
                    condition.getDueDateTo(),
                    pageable
            );
        } else {
            tasks = taskRepository.findTasksByCategory(
                    projectId,
                    condition.getCategoryId(),
                    condition.getStatus(),
                    condition.getDifficulty(),
                    condition.getPriority(),
                    condition.getDueDateFrom(),
                    condition.getDueDateTo(),
                    pageable
            );
        }

        Page<TaskResponse> taskPage = tasks.map(TaskResponse::new);

        return new PageResponse<>(taskPage);
    }

    /**
     * 프로젝트의 작업 목록 전체 조회 (마감일 오름차순)
     * @param memberId 조회하는 회원
     * @param projectId 작업이 속한 프로젝트
     * @return 작업 목록
     */
    public List<TaskResponse> getTasks(Long memberId, Long projectId) {
        Project project = projectSupport.getProjectById(projectId);
        projectSupport.validateOwner(memberId, project);

        return taskRepository.findByProjectIdOrderByDueDateAsc(projectId)
                .stream()
                .map(TaskResponse::new)
                .toList();
    }

    /**
     * 작업 단건 조회
     * @param memberId 조회하는 회원
     * @param projectId 작업이 속한 프로젝트
     * @param taskId 조회할 작업
     * @return 작업 정보
     */
    public TaskResponse getTask(Long memberId, Long projectId, Long taskId) {
        Project project = projectSupport.getProjectById(projectId);
        projectSupport.validateOwner(memberId, project);
        Task task = getTaskByIdAndProjectId(taskId, projectId);
        return new TaskResponse(task);
    }

    /**
     * 작업 수정
     * @param memberId 작업을 수정하는 회원
     * @param projectId 작업이 속한 프로젝트
     * @param taskId 수정할 작업
     * @param request 작업 수정 요청 DTO
     */
    @CacheEvict(
            value = "dashboard",
            key = "#projectId"
    )
    @Transactional
    public void updateTask(
            Long memberId,
            Long projectId,
            Long taskId,
            TaskUpdateRequest request
    ) {
        Project project = projectSupport.getProjectById(projectId);
        projectSupport.validateOwner(memberId, project);
        Task task = getTaskByIdAndProjectId(taskId, projectId);

        task.update(
                request.getTitle(),
                request.getGoal(),
                request.getWorkProcess(),
                request.getLessonLearned(),
                request.getStartedAt(),
                request.getFinishedAt(),
                request.getDueDate(),
                request.getStatus(),
                request.getDifficulty(),
                request.getPriority()
        );

        if (request.getCategoryIds() != null) {
            List<Category> categories = categorySupport.getValidatedCategoriesInProject(
                    projectId,
                    request.getCategoryIds()
            );
            replaceCategories(task, categories);
        }
    }

    /**
     * 작업 삭제
     * @param memberId 작업을 삭제하는 회원
     * @param projectId 작업이 속한 프로젝트
     * @param taskId 삭제할 작업
     */
    @CacheEvict(
            value = "dashboard",
            key = "#projectId"
    )
    @Transactional
    public void deleteTask(Long memberId, Long projectId, Long taskId) {
        Project project = projectSupport.getProjectById(projectId);
        projectSupport.validateOwner(memberId, project);
        Task task = getTaskByIdAndProjectId(taskId, projectId);
        replaceCategories(task, List.of());
        taskRepository.delete(task);
    }

    /**
     * 특정 프로젝트에 속한 작업 조회
     */
    private Task getTaskByIdAndProjectId(Long taskId, Long projectId) {
        return taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.TASK_NOT_FOUND)
                );
    }

    /**
     * 기존 연결된 카테고리를 제거하고 새로 업데이트
     * @param task 업데이트할 작업
     * @param newCategories 업데이트할 카테고리
     */
    private void replaceCategories(
            Task task,
            List<Category> newCategories
    ) {
        List<Category> currentCategories = new ArrayList<>(task.getCategories());
        currentCategories.forEach(category -> category.removeTask(task));
        newCategories.forEach(category -> category.addTask(task));
    }

    /**
     * 페이지 검증
     * @param page 페이지 번호
     * @param size 페이지 크기
     */
    private void validatePageRequest(int page, int size) {
        if (page < 0) {
            throw new BusinessException(ErrorCode.INVALID_PAGE_NUMBER);
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_PAGE_SIZE);
        }
    }

    /**
     * 마감일 검증
     * @param condition 검색 조건
     */
    private void validateSearchCondition(TaskSearchCondition condition) {
        if (condition.getDueDateFrom() != null
                && condition.getDueDateTo() != null
                && condition.getDueDateFrom().isAfter(condition.getDueDateTo())) {
            throw new BusinessException(ErrorCode.INVALID_DUE_DATE_RANGE);
        }
    }
}