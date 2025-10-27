package knu.team1.be.boost.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.notification.dto.NotificationReadResponseDto;
import knu.team1.be.boost.notification.dto.NotificationResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Notifications", description = "알림 관련 API")
@RequestMapping("/api/notifications")
@SecurityRequirement(name = "bearerAuth")
public interface NotificationApi {

    @Operation(
        summary = "내 알림 목록 조회",
        description = "로그인한 사용자의 알림 목록을 최신순으로 반환합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = NotificationResponseDto.class)))
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 멤버", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @GetMapping
    ResponseEntity<List<NotificationResponseDto>> getNotifications(
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
    @PatchMapping("/{notificationId}/read")
    ResponseEntity<NotificationReadResponseDto> markAsRead(
        @PathVariable UUID notificationId,
        @AuthenticationPrincipal UserPrincipalDto user
    );

}
