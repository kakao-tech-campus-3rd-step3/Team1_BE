package knu.team1.be.boost.notification.controller;

import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.notification.dto.NotificationReadResponseDto;
import knu.team1.be.boost.notification.dto.NotificationResponseDto;
import knu.team1.be.boost.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;

    @Override
    public ResponseEntity<List<NotificationResponseDto>> getNotifications(
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        List<NotificationResponseDto> response = notificationService.getNotifications(user);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<NotificationReadResponseDto> markAsRead(
        @PathVariable UUID notificationId,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        NotificationReadResponseDto response = notificationService.markAsRead(notificationId, user);
        return ResponseEntity.ok(response);
    }
}
