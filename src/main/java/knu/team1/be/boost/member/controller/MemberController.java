package knu.team1.be.boost.member.controller;

import jakarta.validation.Valid;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.member.dto.MemberAvatarUpdateRequestDto;
import knu.team1.be.boost.member.dto.MemberNameUpdateRequestDto;
import knu.team1.be.boost.member.dto.MemberResponseDto;
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
    public ResponseEntity<MemberResponseDto> updateMyName(
        @Valid @RequestBody MemberNameUpdateRequestDto requestDto,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        MemberResponseDto updatedMemberInfo = memberService.updateMemberName(user.id(), requestDto);
        return ResponseEntity.ok(updatedMemberInfo);
    }

    @Override
    public ResponseEntity<MemberResponseDto> updateMyAvatar(
        @Valid @RequestBody MemberAvatarUpdateRequestDto requestDto,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        MemberResponseDto updatedMemberInfo = memberService.updateMemberAvatar(user.id(),
            requestDto);
        return ResponseEntity.ok(updatedMemberInfo);
    }

    @Override
    public ResponseEntity<Void> deleteMyAccount(@AuthenticationPrincipal UserPrincipalDto user) {
        memberService.deleteMember(user.id());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> setNotificationEnabled(
        boolean enabled,
        UserPrincipalDto user
    ) {
        memberService.setNotificationEnabled(user.id(), enabled);
        return ResponseEntity.ok().build();
    }
}
