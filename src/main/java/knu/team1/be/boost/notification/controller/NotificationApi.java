package knu.team1.be.boost.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.notification.dto.NotificationListResponseDto;
import knu.team1.be.boost.notification.dto.NotificationReadResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Notifications", description = "알림 관련 API")
@RequestMapping("/api")
@SecurityRequirement(name = "bearerAuth")
public interface NotificationApi {

    @Operation(
        summary = "내 알림 목록 조회 (커서 기반 페이지네이션)",
        description = "로그인한 사용자의 알림 목록을 최신순으로 커서 기반 페이지네이션 방식으로 반환합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = NotificationListResponseDto.class))
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 멤버", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @GetMapping("/notifications")
    ResponseEntity<NotificationListResponseDto> getNotifications(
        @RequestParam(required = false) UUID cursor,
        @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit,
        @AuthenticationPrincipal UserPrincipalDto user
    );

    @Operation(
        summary = "알림 읽음 처리",
        description = "특정 알림을 읽음(read=true) 상태로 변경하고 결과를 반환합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "읽음 처리 성공",
            content = @Content(schema = @Schema(implementation = NotificationReadResponseDto.class))
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "다른 사용자의 알림 접근 시도", content = @Content),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 알림 ID", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @PatchMapping("/notifications/{notificationId}/read")
    ResponseEntity<NotificationReadResponseDto> markAsRead(
        @PathVariable UUID notificationId,
        @AuthenticationPrincipal UserPrincipalDto user
    );

    @Operation(
        summary = "프로젝트별 알림 설정 변경",
        description = "특정 프로젝트에서 사용자의 알림 수신 여부를 켜거나 끕니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "알림 설정 변경 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "프로젝트 또는 멤버십 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PatchMapping("/projects/{projectId}/notifications")
    ResponseEntity<Void> setProjectNotification(
        @PathVariable UUID projectId,
        @RequestParam boolean enabled,
        @AuthenticationPrincipal UserPrincipalDto user
    );

}
