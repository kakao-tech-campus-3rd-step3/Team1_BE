package knu.team1.be.boost.webPush.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "웹 푸시 구독 등록 요청 DTO")
public record WebPushRegisterDto(

    @Schema(description = "웹푸시 세션 토큰", example = "2f8f2a6e-0c48-4e0f-9f5f-123456789abc")
    @NotBlank(message = "세션 토큰은 필수입니다.")
    String token,

    @Schema(description = "푸시 구독 URL (브라우저에서 생성된 endpoint)")
    @NotBlank(message = "웹푸시 URL은 필수입니다.")
    String webPushUrl,

    @Schema(description = "푸시 구독 공개키 (p256dh)")
    @NotBlank(message = "publicKey는 필수입니다.")
    String publicKey,

    @Schema(description = "푸시 구독 인증 키 (auth)")
    @NotBlank(message = "authKey는 필수입니다.")
    String authKey

) {

}
