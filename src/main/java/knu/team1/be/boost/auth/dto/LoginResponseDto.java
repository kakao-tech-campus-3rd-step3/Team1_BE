package knu.team1.be.boost.auth.dto;

import knu.team1.be.boost.member.dto.MemberResponseDto;

public record LoginResponseDto(
    MemberResponseDto memberResponseDto,
    String accessToken,
    Boolean isNewUser
) {

}
