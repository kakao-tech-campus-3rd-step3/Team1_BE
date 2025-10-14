package knu.team1.be.boost.auth.dto;

import knu.team1.be.boost.member.dto.MemberResponseDto;

public record LoginDto(
    MemberResponseDto memberResponseDto,
    TokenDto tokenDto
) {

    public static LoginDto of(
        MemberResponseDto memberResponseDto,
        TokenDto tokenDto
    ) {
        return new LoginDto(
            memberResponseDto,
            tokenDto
        );
    }
}
