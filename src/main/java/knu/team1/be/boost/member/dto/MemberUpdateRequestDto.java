package knu.team1.be.boost.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import knu.team1.be.boost.common.validation.AtLeastOneNotNull;

@AtLeastOneNotNull
public record MemberUpdateRequestDto(
    @Schema(description = "수정할 회원의 이름", example = "김부스트")
    @Size(min = 1, max = 20, message = "이름은 1자 이상 20자 이하로 입력해주세요.")
    String name,

    @Schema(description = "수정할 프로필 이모지", example = "😎")
    @Size(min = 1, message = "프로필 이모지는 비워둘 수 없습니다.")
    String profileEmoji
) {

}
