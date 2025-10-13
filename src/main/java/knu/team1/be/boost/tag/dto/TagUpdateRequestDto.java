package knu.team1.be.boost.tag.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "태그 수정 요청 DTO")
public record TagUpdateRequestDto(
    @NotBlank(message = "태그 이름은 비어있을 수 없습니다.")
    @Size(max = 50, message = "태그 이름은 최대 50자까지 가능합니다.")
    String name
) {

}
