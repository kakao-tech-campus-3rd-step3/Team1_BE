package knu.team1.be.boost.webPush.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "웹푸시 세션 응답 DTO")
public record WebPushSessionResponseDto(
    @Schema(description = "생성된 세션 토큰", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    String token,
    @Schema(description = "세션 상태", example = "CREATED")
    WebPushSessionStatus status
) {

    public static WebPushSessionResponseDto from(String token, WebPushSessionStatus status) {
        return new WebPushSessionResponseDto(token, status);
    }
}
