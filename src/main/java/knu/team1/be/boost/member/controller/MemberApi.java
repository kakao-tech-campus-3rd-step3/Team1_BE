package knu.team1.be.boost.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import knu.team1.be.boost.member.dto.MemberResponseDto;
import knu.team1.be.boost.member.dto.MemberUpdateRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Members", description = "Member 관련 API")
@RequestMapping("/api/members")
public interface MemberApi {

    @Operation(
        summary = "내 정보 조회",
        description = "로그인한 회원의 정보를 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "회원 정보 조회 성공",
            content = @Content(schema = @Schema(implementation = MemberResponseDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @GetMapping("/me")
    ResponseEntity<MemberResponseDto> getMyInfo();

    @Operation(
        summary = "내 정보 수정",
        description = "로그인한 회원의 이름 또는 프로필 이모지를 수정합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "회원 정보 수정 성공",
            content = @Content(schema = @Schema(implementation = MemberResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @PatchMapping("/me")
    ResponseEntity<MemberResponseDto> updateMyInfo(
        @Valid @RequestBody MemberUpdateRequestDto requestDto
    );

    @Operation(
        summary = "회원 탈퇴",
        description = "로그인한 회원의 계정을 탈퇴(soft delete) 처리합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "회원 탈퇴 성공", content = @Content),
        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @DeleteMapping("/me")
    ResponseEntity<Void> deleteMyAccount();
}
