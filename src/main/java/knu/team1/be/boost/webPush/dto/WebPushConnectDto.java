package knu.team1.be.boost.webPush.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "웹 푸시 구독/연결 요청 DTO")
public record WebPushConnectDto(

    @Schema(description = "웹푸시 세션 토큰", example = "2f8f2a6e-0c48-4e0f-9f5f-123456789abc")
    @NotBlank(message = "세션 토큰은 필수입니다.")
    String token,

    @Schema(description = "디바이스 정보")
    @NotBlank(message = "디바이스 정보는 필수입니다.")
    String deviceInfo

) {

}
