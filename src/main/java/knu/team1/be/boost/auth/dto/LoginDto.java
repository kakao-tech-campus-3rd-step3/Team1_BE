package knu.team1.be.boost.auth.dto;

import knu.team1.be.boost.member.dto.MemberResponseDto;

public record LoginDto(
    MemberResponseDto memberResponseDto,
    TokenDto tokenDto,
    Boolean isNewUser
) {

}
