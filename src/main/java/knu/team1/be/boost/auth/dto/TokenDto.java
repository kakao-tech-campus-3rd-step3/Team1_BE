package knu.team1.be.boost.auth.dto;

import lombok.Builder;

@Builder
public record TokenDto(
    String accessToken,
    String refreshToken
) {

}
