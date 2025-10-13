package knu.team1.be.boost.auth.dto;

public record LoginRequestDto(
    String code,
    String redirectUri
) {

}
