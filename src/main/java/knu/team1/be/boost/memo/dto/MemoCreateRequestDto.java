package knu.team1.be.boost.memo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemoCreateRequestDto(
    @Schema(description = "메모 제목", example = "메모 제목 1")
    @NotBlank(message = "제목은 비워둘 수 없습니다.")
    @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
    String title,

    @Schema(description = "메모 내용", example = "메모 내용입니다.")
    @NotBlank(message = "내용은 비워둘 수 없습니다.")
    @Size(max = 10000, message = "메모 내용은 10000자를 초과할 수 없습니다.")
    String content
) {

}
