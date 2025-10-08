package knu.team1.be.boost.comment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.comment.dto.CommentCreateRequestDto;
import knu.team1.be.boost.comment.dto.CommentResponseDto;
import knu.team1.be.boost.comment.dto.CommentUpdateRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Comments", description = "댓글 관련 API")
@RequestMapping("/api")
@SecurityRequirement(name = "bearerAuth")
public interface CommentApi {

    @Operation(summary = "과제 댓글 목록 조회", description = "할 일에 달린 모든 댓글을 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "댓글 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = List.class))
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "프로젝트에 접근할 권한이 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "프로젝트 또는 할 일을 찾을 수 없음", content = @Content)
    })
    @GetMapping("/projects/{projectId}/tasks/{taskId}/comments")
    ResponseEntity<List<CommentResponseDto>> getComments(
        @Parameter(description = "프로젝트 ID") @PathVariable UUID projectId,
        @Parameter(description = "과제 ID") @PathVariable UUID taskId,
        @AuthenticationPrincipal UserPrincipalDto user
    );

    @Operation(summary = "댓글 생성", description = "특정 과제(Task)에 새로운 댓글을 작성합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "댓글 생성 성공",
            content = @Content(schema = @Schema(implementation = CommentResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "프로젝트에 접근할 권한이 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "프로젝트 또는 할 일을 찾을 수 없음", content = @Content)
    })
    @PostMapping("/projects/{projectId}/tasks/{taskId}/comments")
    ResponseEntity<CommentResponseDto> createComment(
        @Parameter(description = "프로젝트 ID") @PathVariable UUID projectId,
        @Parameter(description = "과제 ID") @PathVariable UUID taskId,
        @AuthenticationPrincipal UserPrincipalDto user,
        @Valid @RequestBody CommentCreateRequestDto request
    );

    @Operation(summary = "댓글 수정", description = "자신이 작성한 댓글의 내용을 수정합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "댓글 수정 성공",
            content = @Content(schema = @Schema(implementation = CommentResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "댓글을 수정할 권한이 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음", content = @Content)
    })
    @PutMapping("/comments/{commentId}")
    ResponseEntity<CommentResponseDto> updateComment(
        @Parameter(description = "수정할 댓글 ID") @PathVariable UUID commentId,
        @AuthenticationPrincipal UserPrincipalDto user,
        @Valid @RequestBody CommentUpdateRequestDto request
    );

    @Operation(summary = "댓글 삭제", description = "자신이 작성한 댓글을 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "댓글 삭제 성공", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "댓글을 삭제할 권한이 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음", content = @Content)
    })
    @DeleteMapping("/comments/{commentId}")
    ResponseEntity<Void> deleteComment(
        @Parameter(description = "삭제할 댓글 ID") @PathVariable UUID commentId,
        @AuthenticationPrincipal UserPrincipalDto user
    );
}
