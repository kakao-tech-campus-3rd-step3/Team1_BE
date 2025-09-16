package knu.team1.be.boost.auth.dto;

public record AccessTokenResponseDto(
    String accessToken
) {

    public static AccessTokenResponseDto from(String accessToken) {
        return new AccessTokenResponseDto(accessToken);
    }
}
