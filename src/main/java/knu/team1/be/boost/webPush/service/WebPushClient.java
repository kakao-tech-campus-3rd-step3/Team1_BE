package knu.team1.be.boost.webPush.service;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.List;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.webPush.entity.WebPushSubscription;
import knu.team1.be.boost.webPush.repository.WebPushRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Utils;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebPushClient {

    @Value("${webpush.vapid.public-key}")
    private String publicKey;

    @Value("${webpush.vapid.private-key}")
    private String privateKey;

    private PushService pushService;
    private final WebPushRepository webPushRepository;

    @PostConstruct
    public void init() throws GeneralSecurityException {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        pushService = new PushService();
        pushService.setPublicKey(Utils.loadPublicKey(publicKey));
        pushService.setPrivateKey(Utils.loadPrivateKey(privateKey));
    }

    public void sendNotification(Member member, String title, String message) {
        List<WebPushSubscription> subscriptions = webPushRepository.findByMemberId(member.getId());

        for (WebPushSubscription sub : subscriptions) {
            try {
                JSONObject payload = new JSONObject();
                payload.put("title", title);
                payload.put("body", message);

                Notification notification = new Notification(
                    sub.getWebPushUrl(),
                    sub.getPublicKey(),
                    sub.getAuthKey(),
                    payload.toString().getBytes(StandardCharsets.UTF_8)
                );

                HttpResponse response = pushService.send(notification);
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode == 404 || statusCode == 410) {
                    webPushRepository.delete(sub);
                    continue;
                }

                if (statusCode >= 400) {
                    log.error(
                        "[{} {}] WebPush 전송 실패 | memberId={}, subId={}, reason={}",
                        statusCode,
                        ErrorCode.WEB_PUSH_ERROR.name(),
                        member.getId(),
                        sub.getId(),
                        response.getStatusLine().getReasonPhrase()
                    );
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error(
                    "[{} {}] WebPush 스레드 인터럽트 | memberId={}, subId={}",
                    500,
                    ErrorCode.WEB_PUSH_ERROR.name(),
                    member.getId(),
                    sub.getId(),
                    e
                );
                return;
            } catch (Exception e) {
                log.error(
                    "[500 {}] WebPush 전송 중 예외 발생 | memberId={}, subId={}, msg={}",
                    ErrorCode.WEB_PUSH_ERROR.name(),
                    member.getId(),
                    sub.getId(),
                    e.getMessage(),
                    e
                );
            }
        }
    }
}
