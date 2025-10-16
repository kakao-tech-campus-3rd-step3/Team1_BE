package knu.team1.be.boost.member.controller;

import jakarta.validation.Valid;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.member.dto.MemberResponseDto;
import knu.team1.be.boost.member.dto.MemberUpdateRequestDto;
import knu.team1.be.boost.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController implements MemberApi {

    private final MemberService memberService;

    @Override
    public ResponseEntity<MemberResponseDto> getMyInfo(
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        MemberResponseDto memberInfo = memberService.getMember(user.id());
        return ResponseEntity.ok(memberInfo);
    }

    @Override
    public ResponseEntity<MemberResponseDto> updateMyInfo(
        @Valid @RequestBody MemberUpdateRequestDto requestDto,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        MemberResponseDto updatedMemberInfo = memberService.updateMember(user.id(), requestDto);
        return ResponseEntity.ok(updatedMemberInfo);
    }

    @Override
    public ResponseEntity<Void> deleteMyAccount(@AuthenticationPrincipal UserPrincipalDto user) {
        memberService.deleteMember(user.id());
        return ResponseEntity.noContent().build();
    }
}
