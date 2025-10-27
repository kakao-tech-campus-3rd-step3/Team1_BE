package knu.team1.be.boost.member.service;

import java.util.UUID;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.member.dto.MemberAvatarUpdateRequestDto;
import knu.team1.be.boost.member.dto.MemberNameUpdateRequestDto;
import knu.team1.be.boost.member.dto.MemberResponseDto;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberResponseDto getMember(UUID memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.MEMBER_NOT_FOUND,
                "memberId: " + memberId
            ));
        return MemberResponseDto.from(member);
    }

    @Transactional
    public MemberResponseDto updateMemberName(
        UUID memberId,
        MemberNameUpdateRequestDto requestDto
    ) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.MEMBER_NOT_FOUND,
                "memberId: " + memberId
            ));
        member.updateName(requestDto.name());
        return MemberResponseDto.from(member);
    }

    @Transactional
    public MemberResponseDto updateMemberAvatar(
        UUID memberId,
        MemberAvatarUpdateRequestDto requestDto
    ) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.MEMBER_NOT_FOUND,
                "memberId: " + memberId
            ));
        member.updateAvatar(requestDto.avatar());
        member.updateBackgroundColor(requestDto.backgroundColor());

        return MemberResponseDto.from(member);
    }

    @Transactional
    public void deleteMember(UUID memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.MEMBER_NOT_FOUND,
                "memberId: " + memberId
            ));
        memberRepository.delete(member);
    }
}
