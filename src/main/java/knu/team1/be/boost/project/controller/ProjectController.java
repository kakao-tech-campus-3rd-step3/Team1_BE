package knu.team1.be.boost.project.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.member.dto.MemberResponseDto;
import knu.team1.be.boost.project.dto.ProjectCreateRequestDto;
import knu.team1.be.boost.project.dto.ProjectResponseDto;
import knu.team1.be.boost.project.dto.ProjectUpdateRequestDto;
import knu.team1.be.boost.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProjectController implements ProjectApi {

    private final ProjectService projectService;

    @Override
    public ResponseEntity<ProjectResponseDto> createProject(
        @Valid ProjectCreateRequestDto requestDto,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        ProjectResponseDto projectResponseDto = projectService.createProject(requestDto, user.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(projectResponseDto);
    }

    @Override
    public ResponseEntity<ProjectResponseDto> getProject(
        UUID projectId,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        ProjectResponseDto projectResponseDto = projectService.getProject(projectId, user.id());
        return ResponseEntity.ok(projectResponseDto);
    }

    @Override
    public ResponseEntity<List<ProjectResponseDto>> getMyProjects(
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        List<ProjectResponseDto> projectResponseDtos = projectService.getMyProjects(user.id());
        return ResponseEntity.ok(projectResponseDtos);
    }

    @Override
    public ResponseEntity<ProjectResponseDto> updateProject(
        UUID projectId,
        @Valid ProjectUpdateRequestDto requestDto,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        ProjectResponseDto projectResponseDto = projectService.updateProject(
            projectId,
            requestDto,
            user.id()
        );
        return ResponseEntity.ok(projectResponseDto);
    }

    @Override
    public ResponseEntity<Void> deleteProject(
        UUID projectId,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        projectService.deleteProject(projectId, user.id());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<MemberResponseDto>> getProjectMembers(
        UUID projectId,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        List<MemberResponseDto> memberResponseDtos = projectService.getProjectMembers(
            projectId,
            user.id()
        );
        return ResponseEntity.ok(memberResponseDtos);
    }
}
