package knu.team1.be.boost.webPush.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "WebPush API", description = "웹 푸시 구독 및 테스트 푸시 전송 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WebPushTestController {

    private final MemberRepository memberRepository;
    private final WebPushRepository webPushRepository;
    private final WebPushClient webPushClient;

    @Value("${webpush.vapid.public-key}")
    private String publicKey;

    @Operation(summary = "WebPush Public Key 제공")
    @GetMapping("/publicKey")
    public Map<String, String> getPublicKey() {
        return Map.of("publicKey", publicKey);
    }

    @Operation(
        summary = "WebPush 구독 등록",
        description = "브라우저에서 받은 Web Push 구독 정보를 등록합니다.",
        requestBody = @RequestBody(
            required = true,
            description = "WebPush 구독 정보 (FCM endpoint, publicKey, authKey)",
            content = @Content(schema = @Schema(implementation = WebPushRegisterRequest.class))
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "구독 등록 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
        }
    )
    @PostMapping("/subscribe")
    public SubscribeResponse subscribe(
        @org.springframework.web.bind.annotation.RequestBody WebPushRegisterRequest request
    ) {
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

        Optional<WebPushSubscription> existingSubOpt =
            webPushRepository.findByWebPushUrl(request.getEndpoint());

        WebPushSubscription subscription = existingSubOpt
            .map(sub -> {
                sub.updateSubscription(
                    request.getEndpoint(),
                    request.getKeys().getP256dh(),
                    request.getKeys().getAuth()
                );
                return sub;
            })
            .orElseGet(() -> WebPushSubscription.builder()
                .member(member)
                .token("auto-" + UUID.randomUUID())
                .deviceInfo("Browser")
                .webPushUrl(request.getEndpoint())
                .publicKey(request.getKeys().getP256dh())
                .authKey(request.getKeys().getAuth())
                .build()
            );

        webPushRepository.save(subscription);

        long subscriberCount = webPushRepository.count();

        return new SubscribeResponse("구독 등록 성공", subscriberCount);
    }

    @Operation(
        summary = "WebPush 푸시 전송",
        description = "등록된 구독자들에게 푸시 알림을 전송합니다.",
        requestBody = @RequestBody(
            required = true,
            description = "푸시 메시지 정보",
            content = @Content(schema = @Schema(implementation = PushMessageRequest.class))
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "푸시 전송 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
        }
    )
    @PostMapping("/push")
    public PushResponse sendPush(
        @org.springframework.web.bind.annotation.RequestBody PushMessageRequest request) {
        List<WebPushSubscription> subscriptions = webPushRepository.findAll();
        subscriptions.forEach(sub ->
            webPushClient.sendNotification(
                sub.getMember(),
                request.getTitle(),
                request.getBody()
            )
        );
        return new PushResponse("푸시 전송 완료", subscriptions.size());
    }

    @Data
    @AllArgsConstructor
    public static class WebPushRegisterRequest {

        private String endpoint;
        private Keys keys;

        @Data
        @AllArgsConstructor
        public static class Keys {

            private String p256dh;
            private String auth;
        }
    }

    @Data
    @AllArgsConstructor
    public static class SubscribeResponse {

        private String message;
        private long subscriberCount;
    }

    @Data
    @AllArgsConstructor
    public static class PushMessageRequest {

        private String title;
        private String body;
        private String icon;
    }

    @Data
    @AllArgsConstructor
    public static class PushResponse {

        private String message;
        private int sentTo;
    }
}
