package knu.team1.be.boost.task.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.task.dto.TaskCreateRequestDto;
import knu.team1.be.boost.task.dto.TaskMemberSectionResponseDto;
import knu.team1.be.boost.task.dto.TaskResponseDto;
import knu.team1.be.boost.task.dto.TaskSortBy;
import knu.team1.be.boost.task.dto.TaskSortDirection;
import knu.team1.be.boost.task.dto.TaskStatusRequestDto;
import knu.team1.be.boost.task.dto.TaskStatusSectionDto;
import knu.team1.be.boost.task.dto.TaskUpdateRequestDto;
import knu.team1.be.boost.task.entity.TaskStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Tasks", description = "할 일 관련 API")
@RequestMapping("/api/projects/{projectId}/tasks")
@SecurityRequirement(name = "bearerAuth")
public interface TaskApi {

    @Operation(
        summary = "할 일 생성",
        description = "새로운 할 일을 생성하고 생성된 Task 정보를 반환합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "할 일 생성 성공",
            content = @Content(schema = @Schema(implementation = TaskResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 멤버 ID 포함", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @PostMapping
    ResponseEntity<TaskResponseDto> createTask(
        @PathVariable UUID projectId,
        @Valid @RequestBody TaskCreateRequestDto request,
        @AuthenticationPrincipal UserPrincipalDto user
    );

    @Operation(
        summary = "할 일 수정",
        description = "할 일을 수정하고 수정된 Task 정보를 반환합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "할 일 수정 성공",
            content = @Content(schema = @Schema(implementation = TaskResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트/할 일/멤버 ID 포함", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @PutMapping("/{taskId}")
    ResponseEntity<TaskResponseDto> updateTask(
        @PathVariable UUID projectId,
        @PathVariable UUID taskId,
        @Valid @RequestBody TaskUpdateRequestDto request,
        @AuthenticationPrincipal UserPrincipalDto user
    );

    @Operation(
        summary = "할 일 삭제",
        description = "특정 프로젝트에 속한 할 일을 삭제합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "할 일 삭제 성공", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트/할 일 ID 포함", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @DeleteMapping("/{taskId}")
    ResponseEntity<Void> deleteTask(
        @PathVariable UUID projectId,
        @PathVariable UUID taskId,
        @AuthenticationPrincipal UserPrincipalDto user
    );

    @Operation(
        summary = "할 일 상태 변경",
        description = "특정 프로젝트에 속한 할 일의 상태만 변경합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "할 일 상태 변경 성공", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트/할 일 ID 포함", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @PatchMapping("/{taskId}")
    ResponseEntity<TaskResponseDto> changeTaskStatus(
        @PathVariable UUID projectId,
        @PathVariable UUID taskId,
        @Valid @RequestBody TaskStatusRequestDto request,
        @AuthenticationPrincipal UserPrincipalDto user
    );

    @Operation(
        summary = "프로젝트별 할 일 목록 조회 - 상태 기준 (커서 페이지네이션)",
        description = """
            특정 상태(TaskStatus)에 해당하는 프로젝트의 할 일 목록을 반환합니다.
            정렬(sortBy/direction) 가능
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = TaskStatusSectionDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "프로젝트/상태 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @GetMapping("/status/{status}/tasks")
    ResponseEntity<TaskStatusSectionDto> listTasksByStatus(
        @PathVariable UUID projectId,
        @PathVariable TaskStatus status,
        @RequestParam(required = false, defaultValue = "CREATED_AT") TaskSortBy sortBy,
        @RequestParam(required = false, defaultValue = "ASC") TaskSortDirection direction,
        @RequestParam(required = false) UUID cursor,
        @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit
    );

    @Operation(
        summary = "프로젝트별 할 일 목록 조회 - 특정 팀원 (커서 페이지네이션)",
        description = """
            특정 팀원의 프로젝트의 할 일 목록을 상태별 섹션으로 반환합니다.
            정렬은 지원하지 않으며, 기본 정렬만 제공됩니다.(생성일자 오름차순)
            DONE 상태는 제공되지 않으며  TODO, PROGRESS, REVIEW로 제공됩니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = TaskMemberSectionResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "프로젝트/멤버 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @GetMapping("/members/{memberId}")
    ResponseEntity<TaskMemberSectionResponseDto> listTasksByMember(
        @PathVariable UUID projectId,
        @PathVariable UUID memberId,
        @RequestParam(required = false) UUID cursor,
        @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit
    );

}
