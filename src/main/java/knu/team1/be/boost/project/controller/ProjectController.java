package knu.team1.be.boost.project.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.project.dto.ProjectCreateRequestDto;
import knu.team1.be.boost.project.dto.ProjectResponseDto;
import knu.team1.be.boost.project.dto.ProjectUpdateRequestDto;
import knu.team1.be.boost.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProjectController implements ProjectApi {

    private final ProjectService projectService;

    @Override
    public ResponseEntity<ProjectResponseDto> createProject(
        @Valid ProjectCreateRequestDto requestDto) {
        ProjectResponseDto projectResponseDto = projectService.createProject(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(projectResponseDto);
    }

    @Override
    public ResponseEntity<ProjectResponseDto> getProject(UUID projectId) {
        ProjectResponseDto projectResponseDto = projectService.getProject(projectId);
        return ResponseEntity.ok(projectResponseDto);
    }

    @Override
    public ResponseEntity<List<ProjectResponseDto>> getMyProjects() {
        List<ProjectResponseDto> projectResponseDtos = projectService.getMyProjects();
        return ResponseEntity.ok(projectResponseDtos);
    }

    @Override
    public ResponseEntity<ProjectResponseDto> updateProject(
        UUID projectId,
        @Valid ProjectUpdateRequestDto requestDto
    ) {
        ProjectResponseDto projectResponseDto = projectService.updateProject(projectId, requestDto);
        return ResponseEntity.ok(projectResponseDto);
    }

    @Override
    public ResponseEntity<Void> deleteProject(UUID projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }
}
