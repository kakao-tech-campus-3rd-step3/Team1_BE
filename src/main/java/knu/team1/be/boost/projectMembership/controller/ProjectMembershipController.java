package knu.team1.be.boost.projectMembership.controller;

import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.projectMembership.dto.ProjectJoinCodeResponseDto;
import knu.team1.be.boost.projectMembership.dto.ProjectJoinRequestDto;
import knu.team1.be.boost.projectMembership.dto.ProjectJoinResponseDto;
import knu.team1.be.boost.projectMembership.service.ProjectMembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProjectMembershipController implements ProjectMembershipApi {

    private final ProjectMembershipService membershipService;

    @Override
    public ResponseEntity<ProjectJoinCodeResponseDto> createProjectJoinCode(
        @PathVariable UUID projectId,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        ProjectJoinCodeResponseDto dto = membershipService.generateCode(projectId, user.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Override
    public ResponseEntity<ProjectJoinCodeResponseDto> getProjectJoinCode(
        @PathVariable UUID projectId,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        ProjectJoinCodeResponseDto dto = membershipService.getCode(projectId, user.id());
        return ResponseEntity.ok(dto);
    }

    @Override
    public ResponseEntity<ProjectJoinResponseDto> joinProject(
        @AuthenticationPrincipal UserPrincipalDto user,
        @RequestBody ProjectJoinRequestDto requestDto
    ) {
        ProjectJoinResponseDto dto = membershipService.joinProject(requestDto, user.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Override
    public ResponseEntity<Void> leaveProject(
        @PathVariable UUID projectId,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        membershipService.leaveProject(projectId, user.id());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> kickMember(
        @PathVariable UUID projectId,
        @PathVariable UUID targetMemberId,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        membershipService.kickMember(projectId, targetMemberId, user.id());
        return ResponseEntity.noContent().build();
    }
}
