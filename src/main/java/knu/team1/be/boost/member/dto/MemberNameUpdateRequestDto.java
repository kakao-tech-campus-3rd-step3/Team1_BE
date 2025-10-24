package knu.team1.be.boost.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberNameUpdateRequestDto(
    @Schema(description = "수정할 회원 이름", example = "김부스트")
    @Size(min = 1, max = 20, message = "이름은 1자 이상 20자 이하로 입력해주세요.")
    @NotBlank(message = "이름은 비워둘 수 없습니다.")
    String name
) {

}
