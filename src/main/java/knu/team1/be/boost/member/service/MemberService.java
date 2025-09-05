package knu.team1.be.boost.member.service;

import java.util.UUID;
import knu.team1.be.boost.member.dto.MemberResponseDto;
import knu.team1.be.boost.member.dto.MemberUpdateRequestDto;

public interface MemberService {

    MemberResponseDto getMember(UUID userId);

    MemberResponseDto updateMember(UUID userId, MemberUpdateRequestDto requestDto);

    void deleteMember(UUID userId);
}
