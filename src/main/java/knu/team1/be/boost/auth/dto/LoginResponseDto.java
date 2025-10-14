package knu.team1.be.boost.auth.dto;

import knu.team1.be.boost.member.dto.MemberResponseDto;

public record LoginResponseDto(
    MemberResponseDto memberResponseDto,
    AccessTokenResponseDto accessTokenResponseDto
) {

    public static LoginResponseDto of(
        MemberResponseDto memberResponseDto,
        AccessTokenResponseDto accessTokenResponseDto
    ) {
        return new LoginResponseDto(
            memberResponseDto,
            accessTokenResponseDto
        );
    }
}
