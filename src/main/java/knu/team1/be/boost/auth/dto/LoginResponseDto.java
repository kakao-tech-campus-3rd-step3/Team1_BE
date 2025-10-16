package knu.team1.be.boost.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import knu.team1.be.boost.member.dto.MemberResponseDto;

public record LoginResponseDto(
    @Schema(description = "사용자 정보")
    MemberResponseDto memberResponseDto,

    @Schema(description = "엑세스 토큰")
    String accessToken,

    @Schema(description = "최초 로그인 여부")
    Boolean isNewUser
) {

}
