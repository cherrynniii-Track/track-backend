package com.track.track.service.project;

import com.track.track.domain.Member;
import com.track.track.domain.Project;
import com.track.track.dto.project.ProjectCreateRequest;
import com.track.track.dto.project.ProjectResponse;
import com.track.track.dto.project.ProjectUpdateRequest;
import com.track.track.exception.BusinessException;
import com.track.track.exception.ErrorCode;
import com.track.track.repository.MemberRepository;
import com.track.track.repository.ProjectRepository;
import com.track.track.service.support.ProjectSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;
    private final ProjectSupport projectSupport;


    /**
     * 프로젝트 생성
     * @param memberId 프로젝트를 생성하는 회원
     * @param request 프로젝트 생성 요청 DTO
     * @return 생성된 프로젝트 ID
     */
    @Transactional
    public ProjectResponse createProject(Long memberId, ProjectCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Project project = Project.builder()
                .member(member)
                .name(request.getName())
                .description(request.getDescription())
                .build();

        projectRepository.save(project);
        return new ProjectResponse(project);
    }

    /**
     * 프로젝트 단건 조회
     * @param memberId 조회하는 회원
     * @param projectId 조회하는 프로젝트
     * @return DTO
     */
    public ProjectResponse getProject(Long memberId, Long projectId) {
        Project project = projectSupport.getProjectById(projectId);
        projectSupport.validateOwner(memberId, project);
        return new ProjectResponse(project);
    }

    /**
     * 프로젝트 목록 조회
     * @param memberId 조회하는 회원 ID
     * @return 프로젝트 리스트
     */
    public List<ProjectResponse> getProjects(Long memberId) {
        return projectRepository.findByMemberId(memberId)
                .stream()
                .map(ProjectResponse::new)
                .toList();
    }

    /**
     * 프로젝트 수정
     * @param memberId 프로젝트 수정하는 멤버
     * @param projectId 수정할 프로젝트
     * @param request 수정 요청 DTO
     */
    @Transactional
    public void updateProject(Long memberId,
                              Long projectId,
                              ProjectUpdateRequest request) {
        Project project = projectSupport.getProjectById(projectId);
        projectSupport.validateOwner(memberId, project);
        project.update(request.getName(), request.getDescription());
    }

    /**
     * 프로젝트 삭제
     * @param memberId 삭제하는 멤버
     * @param projectId 삭제할 프로젝트
     */
    @Transactional
    public void deleteProject(Long memberId, Long projectId) {
        Project project = projectSupport.getProjectById(projectId);
        projectSupport.validateOwner(memberId, project);
        projectRepository.delete(project);
    }
}
