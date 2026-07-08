package com.track.track.service.support;

import com.track.track.domain.Project;
import com.track.track.exception.BusinessException;
import com.track.track.exception.ErrorCode;
import com.track.track.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectSupport {

    private final ProjectRepository projectRepository;

    public Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }

    public void validateOwner(Long memberId, Project project) {
        if (!project.getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.PROJECT_ACCESS_DENIED);
        }
    }
}
