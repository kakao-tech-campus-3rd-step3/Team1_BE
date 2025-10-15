package knu.team1.be.boost.memo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemoUpdateRequestDto(
    @NotBlank(message = "제목은 비워둘 수 없습니다.")
    @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
    String title,

    @NotBlank(message = "내용은 비워둘 수 없습니다.")
    String content
) {

}
