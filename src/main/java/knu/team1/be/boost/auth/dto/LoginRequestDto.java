package knu.team1.be.boost.auth.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

@Schema(description = "로그인 요청 DTO")
public record LoginRequestDto(

    @Schema(description = "인가 코드")
    @NotBlank(message = "인가 코드는 필수입니다.")
    String code,

    @Schema(description = "리다이렉트 URI")
    @NotBlank(message = "리다이렉트 URI는 필수입니다.")
    @URL(message = "리다이렉트 URI는 올바른 URL 형식이어야 합니다.")
    String redirectUri
) {

}
