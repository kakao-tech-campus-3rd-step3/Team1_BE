package knu.team1.be.boost.webPush.dto;

import java.util.UUID;
import lombok.Builder;

@Builder
public record WebPushSession(
    String token,
    UUID userId,
    WebPushSessionStatus status,
    String deviceInfo,
    String webPushUrl,
    String publicKey,
    String authKey
) {

}
