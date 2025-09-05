package knu.team1.be.boost.member.service;

import java.util.UUID;
import knu.team1.be.boost.common.exception.MemberNotFoundException;
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
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    @Override
    public MemberResponseDto getMember(UUID userId) {
        Member member = memberRepository.findById(userId)
            .orElseThrow(() -> new MemberNotFoundException(userId));
        return MemberResponseDto.from(member);
    }

    @Transactional
    @Override
    public MemberResponseDto updateMember(UUID userId, MemberUpdateRequestDto requestDto) {
        Member member = memberRepository.findById(userId)
            .orElseThrow(() -> new MemberNotFoundException(userId));
        member.updateMember(requestDto.name(), requestDto.profileEmoji());
        return MemberResponseDto.from(member);
    }

    @Transactional
    @Override
    public void deleteMember(UUID userId) {
        Member member = memberRepository.findById(userId)
            .orElseThrow(() -> new MemberNotFoundException(userId));
        memberRepository.delete(member);
    }
}
