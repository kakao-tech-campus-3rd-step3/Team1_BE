package knu.team1.be.boost.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import knu.team1.be.boost.ai.dto.AiCommentTransformRequestDto;
import knu.team1.be.boost.ai.dto.AiCommentTransformResponseDto;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "AI Comment", description = "AI 댓글 변환 API")
@RequestMapping("/api/ai/comments")
@SecurityRequirement(name = "bearerAuth")
public interface AiCommentApi {

    @Operation(
        summary = "AI 댓글 변환",
        description = "사용자가 작성한 댓글을 지정된 페르소나에 맞춰 AI가 변환합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "댓글 변환 성공",
            content = @Content(schema = @Schema(implementation = AiCommentTransformResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @PostMapping("/transform")
    ResponseEntity<AiCommentTransformResponseDto> transformComment(
        @AuthenticationPrincipal UserPrincipalDto user,
        @Valid @RequestBody AiCommentTransformRequestDto request
    );
}

