package knu.team1.be.boost.project.service;

import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.project.ProjectNotFoundException;
import knu.team1.be.boost.project.dto.ProjectCreateRequestDto;
import knu.team1.be.boost.project.dto.ProjectResponseDto;
import knu.team1.be.boost.project.dto.ProjectUpdateRequestDto;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.project.repository.ProjectRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private static final int DEFAULT_REVIEWER_COUNT = 2;

    private final ProjectRepository projectRepository;

    @Transactional
    public ProjectResponseDto createProject(ProjectCreateRequestDto requestDto) {
        Project project = Project.builder()
            .name(requestDto.name())
            .defaultReviewerCount(DEFAULT_REVIEWER_COUNT).build();
        Project savedProject = projectRepository.save(project);
        return ProjectResponseDto.from(savedProject);
    }

    public ProjectResponseDto getProject(UUID projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));
        return ProjectResponseDto.from(project);
    }

    // Todo: 유저 <-> 프로젝트 연관관계 설정 후 구현
    public List<ProjectResponseDto> getMyProjects() {
        return null;
    }

    @Transactional
    public ProjectResponseDto updateProject(UUID projectId, ProjectUpdateRequestDto requestDto) {
        Project oldProject = projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));

        oldProject.updateProject(requestDto.name(), requestDto.defaultReviewerCount());

        return ProjectResponseDto.from(oldProject);
    }

    @Transactional
    public void deleteProject(UUID projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));
        projectRepository.delete(project);
    }
}
