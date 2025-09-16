package knu.team1.be.boost.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "할 일 상태 변경 요청 DTO")
public record TaskStatusRequestDto(

    @Schema(description = "수정할 할 일 상태 (TODO/PROGRESS/REVIEW/DONE)", example = "REVIEW")
    @NotBlank(message = "상태는 필수입니다.")
    String Status
) {

}
