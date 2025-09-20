package knu.team1.be.boost.member.service;

import java.util.UUID;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.member.dto.MemberResponseDto;
import knu.team1.be.boost.member.dto.MemberUpdateRequestDto;
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

    public MemberResponseDto getMember(UUID userId) {
        Member member = memberRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.MEMBER_NOT_FOUND,
                "userId: " + userId
            ));
        return MemberResponseDto.from(member);
    }

    @Transactional
    public MemberResponseDto updateMember(UUID userId, MemberUpdateRequestDto requestDto) {
        Member member = memberRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.MEMBER_NOT_FOUND,
                "userId: " + userId
            ));
        member.updateMember(requestDto.name(), requestDto.avatar());
        return MemberResponseDto.from(member);
    }

    @Transactional
    public void deleteMember(UUID userId) {
        Member member = memberRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.MEMBER_NOT_FOUND,
                "userId: " + userId
            ));
        memberRepository.delete(member);
    }
}
