package knu.team1.be.boost.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProjectUpdateRequestDto(
    @Schema(description = "프로젝트의 변경할 이름", example = "카테캠 BOOST")
    @NotBlank(message = "프로젝트 이름은 공백일 수 없습니다.")
    @Size(min = 1, max = 30, message = "프로젝트 이름은 1자 이상 30자 이하로 입력해주세요.")
    String name,
    
    @Schema(description = "프로젝트 생성/수정 시 기본으로 설정되는 리뷰어 인원 수", example = "2")
    @NotNull
    @PositiveOrZero
    Integer defaultReviewerCount
) {

}
