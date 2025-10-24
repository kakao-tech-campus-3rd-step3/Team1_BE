package knu.team1.be.boost.webPush.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import java.util.UUID;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.member.entity.vo.OauthInfo;
import knu.team1.be.boost.member.repository.MemberRepository;
import knu.team1.be.boost.webPush.entity.WebPushSubscription;
import knu.team1.be.boost.webPush.repository.WebPushRepository;
import knu.team1.be.boost.webPush.service.WebPushClient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "WebPush API", description = "웹 푸시 구독 및 테스트 푸시 전송 API")
@RestController
@RequestMapping("/api/web-push")
@RequiredArgsConstructor
public class WebPushRegisterController {

    private final MemberRepository memberRepository;
    private final WebPushRepository webPushRepository;
    private final WebPushClient webPushClient;

    @Operation(
        summary = "WebPush 구독 등록 및 테스트 푸시 전송",
        description = """
            브라우저에서 받은 Web Push 구독 정보를 등록하고,
            이미 존재하는 경우 덮어씁니다.
            등록 직후 테스트 푸시 알림을 전송합니다.
            """,
        requestBody = @RequestBody(
            required = true,
            description = "WebPush 구독 정보 (FCM endpoint, publicKey, authKey)",
            content = @Content(schema = @Schema(implementation = WebPushRegisterRequest.class))
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "구독 등록 및 테스트 푸시 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
        }
    )
    @PostMapping("/register")
    public String registerAndPush(
        @org.springframework.web.bind.annotation.RequestBody WebPushRegisterRequest request
    ) {
        // 1️⃣ 테스트용 가상 Member (항상 동일 ID 재사용)
        Member member = memberRepository.findByName("TestUser")
            .orElseGet(() -> {
                OauthInfo fakeOauth = OauthInfo.builder()
                    .provider("test")
                    .providerId(0L)
                    .build();

                Member newMember = Member.builder()
                    .oauthInfo(fakeOauth)
                    .name("TestUser")
                    .avatar("https://example.com/avatar.png")
                    .build();

                return memberRepository.save(newMember);
            });

        // 2️⃣ 기존 구독이 있으면 업데이트, 없으면 새로 생성
        Optional<WebPushSubscription> existingSubOpt =
            webPushRepository.findByWebPushUrl(request.getWebPushUrl());

        WebPushSubscription subscription = existingSubOpt
            .map(sub -> {
                sub.updateSubscription(request.getWebPushUrl(),
                    request.getPublicKey(),
                    request.getAuthKey());
                return sub;
            })
            .orElseGet(() -> WebPushSubscription.builder()
                .member(member)
                .token("auto-" + UUID.randomUUID())
                .deviceInfo("Browser")
                .webPushUrl(request.getWebPushUrl())
                .publicKey(request.getPublicKey())
                .authKey(request.getAuthKey())
                .build()
            );

        webPushRepository.save(subscription);

        // 3️⃣ 테스트 푸시 발송
        webPushClient.sendNotification(
            member,
            "등록 완료 🎉",
            "WebPush 구독이 성공적으로 등록되었습니다."
        );

        return "✅ WebPush 등록 및 테스트 푸시 완료 (userId=" + member.getId() + ")";
    }

    @Data
    @AllArgsConstructor
    @Schema(description = "WebPush 구독 등록 요청 DTO")
    public static class WebPushRegisterRequest {

        @Schema(description = "브라우저에서 발급받은 FCM endpoint URL")
        private String webPushUrl;

        @Schema(description = "브라우저 푸시 구독 publicKey (p256dh)")
        private String publicKey;

        @Schema(description = "브라우저 푸시 구독 authKey")
        private String authKey;
    }
}
