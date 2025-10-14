package knu.team1.be.boost.project.service;

import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.common.policy.AccessPolicy;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.member.repository.MemberRepository;
import knu.team1.be.boost.project.dto.ProjectCreateRequestDto;
import knu.team1.be.boost.project.dto.ProjectResponseDto;
import knu.team1.be.boost.project.dto.ProjectUpdateRequestDto;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.project.repository.ProjectRepository;
import knu.team1.be.boost.projectMembership.entity.ProjectMembership;
import knu.team1.be.boost.projectMembership.entity.ProjectRole;
import knu.team1.be.boost.projectMembership.repository.ProjectMembershipRepository;
import knu.team1.be.boost.tag.repository.TagRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private static final int DEFAULT_REVIEWER_COUNT = 2;

    private final TagRepository tagRepository;
    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final AccessPolicy accessPolicy;

    @Transactional
    public ProjectResponseDto createProject(ProjectCreateRequestDto requestDto, UUID memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.MEMBER_NOT_FOUND,
                "memberId: " + memberId
            ));

        Project project = Project.builder()
            .name(requestDto.name())
            .defaultReviewerCount(DEFAULT_REVIEWER_COUNT).build();

        Project savedProject = projectRepository.save(project);
        ProjectMembership projectMembership = ProjectMembership.createProjectMembership(
            savedProject,
            member,
            ProjectRole.OWNER
        );
        projectMembershipRepository.save(projectMembership);
        return ProjectResponseDto.from(savedProject);
    }

    public ProjectResponseDto getProject(UUID projectId, UUID memberId) {

        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PROJECT_NOT_FOUND,
                "projectId: " + projectId
            ));

        accessPolicy.ensureProjectMember(projectId, memberId);

        return ProjectResponseDto.from(project);
    }

    public List<ProjectResponseDto> getMyProjects(UUID memberId) {

        List<ProjectMembership> projectMemberships = projectMembershipRepository.findAllByMemberId(
            memberId);

        return projectMemberships.stream()
            .map(ProjectMembership::getProject)
            .map(ProjectResponseDto::from)
            .toList();
    }

    @Transactional
    public ProjectResponseDto updateProject(
        UUID projectId,
        ProjectUpdateRequestDto requestDto,
        UUID memberId
    ) {

        Project oldProject = projectRepository.findById(projectId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PROJECT_NOT_FOUND,
                "projectId: " + projectId
            ));

        accessPolicy.ensureProjectOwner(projectId, memberId);

        oldProject.updateProject(requestDto.name(), requestDto.defaultReviewerCount());

        return ProjectResponseDto.from(oldProject);
    }

    @Transactional
    public void deleteProject(UUID projectId, UUID memberId) {

        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PROJECT_NOT_FOUND,
                "projectId: " + projectId
            ));

        accessPolicy.ensureProjectOwner(projectId, memberId);
        tagRepository.deleteAllByProjectId(projectId);
        projectRepository.delete(project);
    }
}
