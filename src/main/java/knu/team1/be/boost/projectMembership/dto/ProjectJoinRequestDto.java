package knu.team1.be.boost.projectMembership.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectJoinRequestDto(

    @Schema(description = "참여할 프로젝트의 참여 코드", example = "A1B2C3")
    @NotBlank(message = "프로젝트 참여 코드는 공백일 수 없습니다.")
    @Size(min = 6, max = 6, message = "프로젝트 참여 코드는 반드시 6자리여야 합니다.")
    String joinCode

) {

}
