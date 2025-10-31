package knu.team1.be.boost.webPush.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.webPush.dto.WebPushConnectDto;
import knu.team1.be.boost.webPush.dto.WebPushRegisterDto;
import knu.team1.be.boost.webPush.dto.WebPushSessionResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "WebPush", description = "웹푸시 관련 API")
@RequestMapping("/api/web-push")
@SecurityRequirement(name = "bearerAuth")
public interface WebPushApi {

    @Operation(summary = "웹푸시 세션 생성", description = "웹푸시 연결 세션을 생성하고 토큰을 반환합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "세션 생성 성공",
            content = @Content(schema = @Schema(implementation = WebPushSessionResponseDto.class))
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PostMapping("/sessions")
    ResponseEntity<WebPushSessionResponseDto> createSession(
        @AuthenticationPrincipal UserPrincipalDto user);

    @Operation(summary = "웹 푸시 디바이스 연결", description = "세션 토큰과 디바이스 정보를 등록하여 연결 상태로 업데이트합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "디바이스 연결 성공",
            content = @Content(schema = @Schema(implementation = WebPushSessionResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 토큰", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PostMapping("/sessions/connect")
    ResponseEntity<WebPushSessionResponseDto> connectDevice(
        @Valid @RequestBody WebPushConnectDto dto);

    @Operation(summary = "웹 푸시 세션 상태 조회", description = "세션 토큰으로 현재 연결 상태를 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "세션 상태 조회 성공",
            content = @Content(schema = @Schema(implementation = WebPushSessionResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 토큰", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @GetMapping("/sessions/{token}")
    ResponseEntity<WebPushSessionResponseDto> getSessionStatus(@PathVariable String token);

    @Operation(summary = "웹푸시 구독 등록", description = "푸시 구독 정보를 등록하거나 이미 등록된 디바이스인 경우 업데이트합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "웹 푸시 등록/업데이트 성공",
            content = @Content(schema = @Schema(implementation = WebPushSessionResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 토큰", content = @Content),
        @ApiResponse(responseCode = "404", description = "멤버 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PostMapping("/subscriptions")
    ResponseEntity<WebPushSessionResponseDto> registerSubscription(
        @Valid @RequestBody WebPushRegisterDto dto
    );
}
