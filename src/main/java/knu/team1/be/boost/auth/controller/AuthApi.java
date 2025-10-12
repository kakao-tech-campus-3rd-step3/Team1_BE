package knu.team1.be.boost.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import knu.team1.be.boost.auth.dto.AccessTokenResponseDto;
import knu.team1.be.boost.auth.dto.LoginRequestDto;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Auth", description = "로그인 관련 API")
@RequestMapping("/api/auth")
public interface AuthApi {

    @Operation(
        summary = "카카오 로그인/회원가입",
        description = "카카오 계정으로 로그인/회원가입을 진행합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = AccessTokenResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @PostMapping("/login/kakao")
    ResponseEntity<AccessTokenResponseDto> kakaoLogin(@RequestBody LoginRequestDto requestDto);

    @Operation(
        summary = "로그아웃",
        description = "계정 로그아웃을 진행합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "로그아웃 성공", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    ResponseEntity<Void> logout(@AuthenticationPrincipal UserPrincipalDto userPrincipalDto);

    @Operation(
        summary = "토큰 재발급",
        description = "만료된 Access Token과 Refresh Token을 재발급합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "토큰 재발급 성공",
            content = @Content(schema = @Schema(implementation = AccessTokenResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "404", description = "리프레시 토큰을 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    @PostMapping("/reissue")
    ResponseEntity<AccessTokenResponseDto> reissue(
        @CookieValue("refreshToken") String refreshToken
    );
}
