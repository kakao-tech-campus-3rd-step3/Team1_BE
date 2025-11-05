package knu.team1.be.boost.notification.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.notification.dto.NotificationCountResponseDto;
import knu.team1.be.boost.notification.dto.NotificationListResponseDto;
import knu.team1.be.boost.notification.dto.NotificationReadResponseDto;
import knu.team1.be.boost.notification.dto.ProjectNotificationResponseDto;
import knu.team1.be.boost.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;

    @Override
    public ResponseEntity<NotificationListResponseDto> getNotifications(
        @RequestParam(required = false) UUID cursor,
        @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        NotificationListResponseDto response = notificationService.getNotifications(
            cursor,
            limit,
            user.id()
        );
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<NotificationReadResponseDto> markAsRead(
        @PathVariable UUID notificationId,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        NotificationReadResponseDto response = notificationService.markAsRead(
            notificationId,
            user.id()
        );
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ProjectNotificationResponseDto> setProjectNotification(
        @PathVariable UUID projectId,
        @RequestParam boolean enabled,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        ProjectNotificationResponseDto response = notificationService.setProjectNotification(
            projectId,
            enabled,
            user.id()
        );
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<NotificationCountResponseDto> getNotificationCount(
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        NotificationCountResponseDto response = notificationService.getNotificationCount(user.id());
        return ResponseEntity.ok(response);
    }


}
