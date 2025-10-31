package knu.team1.be.boost.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MemberAvatarUpdateRequestDto(
    @Schema(description = "수정할 회원 아바타", example = "1112")
    @NotBlank(message = "아바타는 비워둘 수 없습니다.")
    String avatar,

    @Schema(description = "수정할 회원 배경색 (Hex 색상 코드)", example = "#FF5733")
    @NotBlank(message = "배경색은 비워둘 수 없습니다.")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "배경색은 #000000 형식의 Hex 색상 코드여야 합니다.")
    String backgroundColor
) {

}
