package knu.team1.be.boost.tag.controller;

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
import knu.team1.be.boost.tag.dto.TagCreateRequestDto;
import knu.team1.be.boost.tag.dto.TagResponseDto;
import knu.team1.be.boost.tag.dto.TagUpdateRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Tags", description = "태그 관련 API")
@RequestMapping("/api")
@SecurityRequirement(name = "bearerAuth")
public interface TagApi {

    @Operation(
        summary = "태그 생성",
        description = "프로젝트에 새 태그를 생성하고 생성된 태그 정보를 반환합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "태그 생성 성공",
            content = @Content(schema = @Schema(implementation = TagResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트", content = @Content),
        @ApiResponse(responseCode = "409", description = "중복 태그 이름", content = @Content)
    })
    @PostMapping("/projects/{projectId}/tags")
    ResponseEntity<TagResponseDto> createTag(
        @PathVariable UUID projectId,
        @Valid @RequestBody TagCreateRequestDto request,
        @AuthenticationPrincipal UserPrincipalDto user
    );

    @Operation(
        summary = "태그 목록 조회",
        description = "프로젝트에 속한 태그들의 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "프로젝트 모든 태그 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = TagResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트", content = @Content)
    })
    @PostMapping("/projects/{projectId}/tags")
    ResponseEntity<List<TagResponseDto>> getAllTags(
        @PathVariable UUID projectId,
        @AuthenticationPrincipal UserPrincipalDto user
    );

    @Operation(
        summary = "태그 수정",
        description = "프로젝트 태그 이름을 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "태그 수정 성공",
            content = @Content(schema = @Schema(implementation = TagResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "프로젝트/태그 없음", content = @Content),
        @ApiResponse(responseCode = "409", description = "중복된 태그 이름", content = @Content)
    })
    @PatchMapping("/projects/{projectId}/tags/{tagId}")
    ResponseEntity<TagResponseDto> updateTag(
        @PathVariable UUID projectId,
        @PathVariable UUID tagId,
        @Valid @RequestBody TagUpdateRequestDto request,
        @AuthenticationPrincipal UserPrincipalDto user
    );

    @Operation(
        summary = "태그 삭제",
        description = "프로젝트의 태그를 삭제합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "태그 삭제 성공", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "프로젝트/태그 없음", content = @Content)
    })
    @DeleteMapping("/projects/{projectId}/tags/{tagId}")
    ResponseEntity<Void> deleteTag(
        @PathVariable UUID projectId,
        @PathVariable UUID tagId,
        @AuthenticationPrincipal UserPrincipalDto user
    );
}
