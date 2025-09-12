package knu.team1.be.boost.member.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import knu.team1.be.boost.member.dto.MemberResponseDto;
import knu.team1.be.boost.member.dto.MemberUpdateRequestDto;
import knu.team1.be.boost.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController implements MemberApi {

    private final MemberService memberService;

    private final UUID memberId = UUID.fromString(
        "a1b2c3d4-e5f6-7890-1234-567890abcdef"); // 테스트용 memberId 임의 생성 -> 로그인 구현시 제거

    @Override
    public ResponseEntity<MemberResponseDto> getMyInfo() {
        MemberResponseDto memberInfo = memberService.getMember(memberId);
        return ResponseEntity.ok(memberInfo);
    }

    @Override
    public ResponseEntity<MemberResponseDto> updateMyInfo(
        @Valid @RequestBody MemberUpdateRequestDto requestDto
    ) {
        MemberResponseDto updatedMemberInfo = memberService.updateMember(memberId, requestDto);
        return ResponseEntity.ok(updatedMemberInfo);
    }

    @Override
    public ResponseEntity<Void> deleteMyAccount() {
        memberService.deleteMember(memberId);
        return ResponseEntity.noContent().build();
    }
}
