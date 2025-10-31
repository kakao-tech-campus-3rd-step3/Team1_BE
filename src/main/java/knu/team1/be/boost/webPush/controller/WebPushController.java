package knu.team1.be.boost.webPush.controller;

import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.webPush.dto.WebPushConnectDto;
import knu.team1.be.boost.webPush.dto.WebPushRegisterDto;
import knu.team1.be.boost.webPush.dto.WebPushSessionResponseDto;
import knu.team1.be.boost.webPush.service.WebPushService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WebPushController implements WebPushApi {

    private final WebPushService webPushService;

    @Override
    public ResponseEntity<WebPushSessionResponseDto> createSession(
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        WebPushSessionResponseDto response = webPushService.createSession(user);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<WebPushSessionResponseDto> connectDevice(WebPushConnectDto dto) {
        WebPushSessionResponseDto response = webPushService.connectDevice(dto);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<WebPushSessionResponseDto> getSessionStatus(String token) {
        WebPushSessionResponseDto response = webPushService.getSessionStatus(token);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<WebPushSessionResponseDto> registerSubscription(WebPushRegisterDto dto) {
        WebPushSessionResponseDto response = webPushService.registerSubscription(dto);
        return ResponseEntity.ok(response);
    }
}
