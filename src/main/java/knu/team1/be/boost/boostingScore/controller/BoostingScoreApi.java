package knu.team1.be.boost.boostingScore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.boostingScore.dto.BoostingScoreResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Boosting Score", description = "프로젝트 공헌도 점수 관련 API")
@RequestMapping("/api/projects/{projectId}/boosting-scores")
@SecurityRequirement(name = "bearerAuth")
public interface BoostingScoreApi {

    @GetMapping
    @Operation(
        summary = "프로젝트 공헌도 점수 조회",
        description = "프로젝트에 참여하고 있는 모든 멤버의 공헌도 점수를 조회합니다. "
            + "점수는 Task 참여, 댓글 작성, Approve 개수를 기반으로 계산됩니다. "
            + "프로젝트 멤버만 조회가 가능합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "공헌도 점수 조회 성공",
            content = @Content(
                array = @ArraySchema(schema = @Schema(implementation = BoostingScoreResponseDto.class))
            )
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "프로젝트 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    ResponseEntity<List<BoostingScoreResponseDto>> getBoostingScores(
        @PathVariable UUID projectId,
        @AuthenticationPrincipal UserPrincipalDto user
    );
}

