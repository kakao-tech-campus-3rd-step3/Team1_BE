package knu.team1.be.boost.projectMembership.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.projectMembership.dto.ProjectJoinCodeResponseDto;
import knu.team1.be.boost.projectMembership.dto.ProjectJoinRequestDto;
import knu.team1.be.boost.projectMembership.dto.ProjectJoinResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "ProjectMembership", description = "Project 참여/떠나기 관련 API")
@SecurityRequirement(name = "bearerAuth")
public interface ProjectMembershipApi {

    @PostMapping("/api/projects/{projectId}/join-code")
    @Operation(
        summary = "프로젝트 참여 코드 생성",
        description = "새 프로젝트 참여 코드를 생성합니다. 기존 코드는 만료됩니다. "
            + "프로젝트에 참여하고 있는 사용자만 생성할 수 있습니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "새 프로젝트 참여 코드 생성 성공",
            content = @Content(schema = @Schema(implementation = ProjectJoinCodeResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "프로젝트 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    ResponseEntity<ProjectJoinCodeResponseDto> createProjectJoinCode(
        @PathVariable UUID projectId,
        @AuthenticationPrincipal UserPrincipalDto user
    );

    @GetMapping("/api/projects/{projectId}/join-code")
    @Operation(
        summary = "프로젝트 참여 코드 조회",
        description = "유효한 프로젝트 참여 코드를 조회합니다. "
            + "프로젝트에 참여하고 있는 사용자만 조회할 수 있습니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "프로젝트 참여 코드 조회 성공",
            content = @Content(schema = @Schema(implementation = ProjectJoinCodeResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "프로젝트 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    ResponseEntity<ProjectJoinCodeResponseDto> getProjectJoinCode(
        @PathVariable UUID projectId,
        @AuthenticationPrincipal UserPrincipalDto user
    );

    @PostMapping("/api/projects/join")
    @Operation(summary = "프로젝트 참여", description = "프로젝트에 참여합니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "프로젝트 참여 성공",
            content = @Content(schema = @Schema(implementation = ProjectJoinResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "프로젝트 없음", content = @Content),
        @ApiResponse(responseCode = "409", description = "이미 참여 중인 사용자", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    ResponseEntity<ProjectJoinResponseDto> joinProject(
        @AuthenticationPrincipal UserPrincipalDto user,
        @Valid @RequestBody ProjectJoinRequestDto projectJoinRequestDto
    );

    @DeleteMapping("/api/projects/{projectId}/leave")
    @Operation(summary = "프로젝트 나가기", description = "프로젝트에서 나갑니다. "
        + "프로젝트 owner는 프로젝트를 나갈 수 없습니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "프로젝트 퇴장 성공",
            content = @Content
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음 (프로젝트 owner는 나갈 수 없음)", content = @Content),
        @ApiResponse(responseCode = "404", description = "프로젝트 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    ResponseEntity<Void> leaveProject(
        @PathVariable UUID projectId,
        @AuthenticationPrincipal UserPrincipalDto user
    );

    @DeleteMapping("/api/projects/{projectId}/members/{targetMemberId}")
    @Operation(summary = "프로젝트 멤버 추방", description = "프로젝트에서 특정 멤버를 추방합니다. "
        + "프로젝트 owner만 사용할 수 있습니다. 자기 자신이나 다른 owner를 추방할 수 없습니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "멤버 추방 성공",
            content = @Content
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (자기 자신을 추방하려는 경우)", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음 (프로젝트 owner만 사용 가능, owner는 추방 불가)", content = @Content),
        @ApiResponse(responseCode = "404", description = "프로젝트 또는 멤버 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    ResponseEntity<Void> kickMember(
        @PathVariable UUID projectId,
        @PathVariable UUID targetMemberId,
        @AuthenticationPrincipal UserPrincipalDto user
    );

}
