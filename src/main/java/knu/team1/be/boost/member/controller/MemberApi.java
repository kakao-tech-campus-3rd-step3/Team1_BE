package knu.team1.be.boost.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.member.dto.MemberAvatarUpdateRequestDto;
import knu.team1.be.boost.member.dto.MemberNameUpdateRequestDto;
import knu.team1.be.boost.member.dto.MemberResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Members", description = "Member 관련 API")
@RequestMapping("/api/members")
@SecurityRequirement(name = "bearerAuth")
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
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @GetMapping("/me")
    ResponseEntity<MemberResponseDto> getMyInfo(@AuthenticationPrincipal UserPrincipalDto user);

    @Operation(
        summary = "내 이름 수정",
        description = "로그인한 회원의 이름을 수정합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "이름 수정 성공",
            content = @Content(schema = @Schema(implementation = MemberResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @PutMapping("/me/name")
    ResponseEntity<MemberResponseDto> updateMyName(
        @Valid @RequestBody MemberNameUpdateRequestDto requestDto,
        @AuthenticationPrincipal UserPrincipalDto user
    );

    @Operation(
        summary = "내 아바타 및 배경색 수정",
        description = "로그인한 회원의 아바타와 배경색을 수정합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "아바타 및 배경색 수정 성공",
            content = @Content(schema = @Schema(implementation = MemberResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @PutMapping("/me/avatar")
    ResponseEntity<MemberResponseDto> updateMyAvatar(
        @Valid @RequestBody MemberAvatarUpdateRequestDto requestDto,
        @AuthenticationPrincipal UserPrincipalDto user
    );

    @Operation(
        summary = "회원 탈퇴",
        description = "로그인한 회원의 계정을 탈퇴(soft delete) 처리합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "회원 탈퇴 성공", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @DeleteMapping("/me")
    ResponseEntity<Void> deleteMyAccount(@AuthenticationPrincipal UserPrincipalDto user);
}
