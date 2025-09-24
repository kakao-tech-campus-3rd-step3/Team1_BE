package knu.team1.be.boost.projectMembership.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import knu.team1.be.boost.projectMembership.dto.ProjectJoinCodeResponseDto;
import knu.team1.be.boost.projectMembership.dto.ProjectJoinRequestDto;
import knu.team1.be.boost.projectMembership.dto.ProjectJoinResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "ProjectMembership", description = "Project 참여/떠나기 관련 API")
@RequestMapping("/api/projects/{projectId}")
@SecurityRequirement(name = "bearerAuth")
public interface ProjectMembershipApi {

    @PostMapping("/join-code")
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
    ResponseEntity<ProjectJoinCodeResponseDto> createProjectJoinCode(@PathVariable UUID projectId);

    @GetMapping("/join-code")
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
    ResponseEntity<ProjectJoinCodeResponseDto> getProjectJoinCode(@PathVariable UUID projectId);

    @PostMapping("/join")
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
        @PathVariable UUID projectId,
        @RequestBody ProjectJoinRequestDto projectJoinRequestDto
    );

    @DeleteMapping("/leave")
    @Operation(summary = "프로젝트 나가기", description = "프로젝트에서 나갑니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "프로젝트 퇴장 성공",
            content = @Content
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "프로젝트 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    ResponseEntity<Void> leaveProject(@PathVariable UUID projectId);

}
