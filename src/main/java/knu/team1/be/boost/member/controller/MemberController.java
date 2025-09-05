package knu.team1.be.boost.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.UUID;
import knu.team1.be.boost.member.dto.MemberResponseDto;
import knu.team1.be.boost.member.dto.MemberUpdateRequestDto;
import knu.team1.be.boost.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    private final UUID memberId = UUID.fromString(
        "a1b2c3d4-e5f6-7890-1234-567890abcdef"); // 테스트용 memberId 임의 생성 -> 로그인 구현시 제거

    @Operation(summary = "내 정보 조회", description = "로그인한 회원의 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = MemberResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음", content = @Content)
    })
    @GetMapping("/me")
    public ResponseEntity<MemberResponseDto> getMyInfo() {
        MemberResponseDto memberInfo = memberService.getMember(memberId);
        return ResponseEntity.ok(memberInfo);
    }

    @Operation(summary = "내 정보 수정", description = "로그인한 회원의 이름 또는 프로필 이모지를 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "수정 성공",
            content = @Content(schema = @Schema(implementation = MemberResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음", content = @Content)
    })
    @PatchMapping("/me")
    public ResponseEntity<MemberResponseDto> updateMyInfo(
        @Valid @RequestBody MemberUpdateRequestDto requestDto
    ) {
        MemberResponseDto updatedMemberInfo = memberService.updateMember(memberId, requestDto);
        return ResponseEntity.ok(updatedMemberInfo);
    }

    @Operation(summary = "회원 탈퇴", description = "로그인한 회원의 계정을 탈퇴(soft delete) 처리합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "회원 탈퇴 성공", content = @Content),
        @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음", content = @Content)
    })
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount() {
        memberService.deleteMember(memberId);
        return ResponseEntity.noContent().build();
    }
}
