package knu.team1.be.boost.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.project.dto.ProjectCreateRequestDto;
import knu.team1.be.boost.project.dto.ProjectResponseDto;
import knu.team1.be.boost.project.dto.ProjectUpdateRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

// Todo: 로그인 된 사용자 권한 검증 추가 필요
@Tag(name = "Project", description = "Project 관련 API")
@RequestMapping("/api/projects")
public interface ProjectApi {

    @PostMapping()
    @Operation(summary = "프로젝트 생성", description = "새 프로젝트를 생성하고 참여합니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "새 프로젝트 생성 성공",
            content = @Content(schema = @Schema(implementation = ProjectResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    ResponseEntity<ProjectResponseDto> createProject(
        @RequestBody @Valid ProjectCreateRequestDto requestDto
    );

    @GetMapping("/{projectId}")
    @Operation(summary = "프로젝트 조회", description = "프로젝트 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "프로젝트 조회 성공",
            content = @Content(schema = @Schema(implementation = ProjectResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "프로젝트 없음", content = @Content)
    })
    ResponseEntity<ProjectResponseDto> getProject(@PathVariable UUID projectId);

    @GetMapping("/me")
    @Operation(
        summary = "참여 프로젝트 조회",
        description = "참여하고 있는 프로젝트 리스트를 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "프로젝트 리스트 조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProjectResponseDto.class)))
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    ResponseEntity<List<ProjectResponseDto>> getMyProjects();

    @PatchMapping("/{projectId}")
    @Operation(summary = "프로젝트 수정", description = "프로젝트의 정보(설정)를 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "프로젝트 정보 수정 성공",
            content = @Content(schema = @Schema(implementation = ProjectResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "프로젝트 없음", content = @Content)
    })
    ResponseEntity<ProjectResponseDto> updateProject(
        @PathVariable UUID projectId,
        @RequestBody @Valid ProjectUpdateRequestDto requestDto
    );

    @DeleteMapping("/{projectId}")
    @Operation(summary = "프로젝트 삭제", description = "프로젝트를 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "프로젝트 삭제 성공",
            content = @Content
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "프로젝트 없음", content = @Content)
    })
    ResponseEntity<Void> deleteProject(@PathVariable UUID projectId);

}
