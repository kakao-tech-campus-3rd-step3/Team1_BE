package knu.team1.be.boost.member.controller;

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
        "a1b2c3d4-e5f6-7890-1234-567890abcdef"); // 테스트용 memberId 임의 생성 -> 카카오 로그인 구현

    @GetMapping("/me")
    public ResponseEntity<MemberResponseDto> getMyInfo() {
        MemberResponseDto memberInfo = memberService.getMember(memberId);
        return ResponseEntity.ok(memberInfo);
    }

    @PatchMapping("/me")
    public ResponseEntity<MemberResponseDto> updateMyInfo(
        @RequestBody MemberUpdateRequestDto requestDto) {
        MemberResponseDto updatedMemberInfo = memberService.updateMember(memberId, requestDto);
        return ResponseEntity.ok(updatedMemberInfo);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount() {
        memberService.deleteMember(memberId);
        return ResponseEntity.noContent().build();
    }
}
