package knu.team1.be.boost.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberUpdateRequestDto(
    @Schema(description = "수정할 회원의 이름", example = "김부스트")
    String name,

    @Schema(description = "수정할 프로필 이모지", example = "😎")
    String profileEmoji
) {

}
