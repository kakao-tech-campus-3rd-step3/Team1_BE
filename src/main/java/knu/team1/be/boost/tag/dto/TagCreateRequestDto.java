package knu.team1.be.boost.tag.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "태그 생성 요청 DTO")
public record TagCreateRequestDto(

    @Schema(description = "태그 이름", example = "피드백")
    @NotBlank(message = "태그 이름은 필수입니다.")
    @Size(max = 50, message = "태그 이름은 최대 50자까지 입력할 수 있습니다.")
    String name

) {}
