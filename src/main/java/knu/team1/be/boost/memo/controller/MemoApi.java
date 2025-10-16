package knu.team1.be.boost.memo.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import knu.team1.be.boost.memo.dto.MemoCreateRequestDto;
import knu.team1.be.boost.memo.dto.MemoItemResponseDto;
import knu.team1.be.boost.memo.dto.MemoResponseDto;
import knu.team1.be.boost.memo.dto.MemoUpdateRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Memo", description = "메모 관련 API")
@RequestMapping("/api/projects/{projectId}")
@SecurityRequirement(name = "bearerAuth")
public interface MemoApi {

    @Operation(summary = "메모 생성 API")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "메모 생성 성공",
            content = @Content(schema = @Schema(implementation = MemoResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @PostMapping("/memos")
    ResponseEntity<MemoResponseDto> createMemo(
        @PathVariable UUID projectId,
        @AuthenticationPrincipal UserPrincipalDto user,
        @Valid @RequestBody MemoCreateRequestDto requestDto
    );

    @Operation(summary = "프로젝트의 메모 목록 조회 API")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "메모 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = List.class))
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "메모를 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @GetMapping("/memos")
    ResponseEntity<List<MemoItemResponseDto>> getMemoList(
        @PathVariable UUID projectId,
        @AuthenticationPrincipal UserPrincipalDto user
    );

    @Operation(summary = "메모 상세 조회 API")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "메모 상세 조회 성공",
            content = @Content(schema = @Schema(implementation = MemoResponseDto.class))
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "메모를 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @GetMapping("/memos/{memoId}")
    ResponseEntity<MemoResponseDto> getMemo(
        @PathVariable UUID projectId,
        @PathVariable UUID memoId,
        @AuthenticationPrincipal UserPrincipalDto user
    );

    @Operation(summary = "메모 수정 API")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "메모 수정 성공",
            content = @Content(schema = @Schema(implementation = MemoResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "메모를 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @PutMapping("/memos/{memoId}")
    ResponseEntity<MemoResponseDto> updateMemo(
        @PathVariable UUID projectId,
        @PathVariable UUID memoId,
        @AuthenticationPrincipal UserPrincipalDto user,
        @Valid @RequestBody MemoUpdateRequestDto requestDto
    );

    @Operation(summary = "메모 삭제 API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "메모 삭제 성공", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "메모를 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @DeleteMapping("/memos/{memoId}")
    ResponseEntity<Void> deleteMemo(
        @PathVariable UUID projectId,
        @PathVariable UUID memoId,
        @AuthenticationPrincipal UserPrincipalDto user
    );
}
