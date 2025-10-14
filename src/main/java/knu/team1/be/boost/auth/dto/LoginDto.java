package knu.team1.be.boost.auth.dto;

import knu.team1.be.boost.member.dto.MemberResponseDto;

public record LoginDto(
    MemberResponseDto memberResponseDto,
    TokenDto tokenDto,
    Boolean isNewUser
) {

    public static LoginDto of(
        MemberResponseDto memberResponseDto,
        TokenDto tokenDto,
        Boolean isNewUser
    ) {
        return new LoginDto(
            memberResponseDto,
            tokenDto,
            isNewUser
        );
    }
}
