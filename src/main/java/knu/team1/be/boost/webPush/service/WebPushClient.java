package knu.team1.be.boost.webPush.service;

import jakarta.annotation.PostConstruct;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.webPush.entity.WebPushSubscription;
import knu.team1.be.boost.webPush.repository.WebPushRepository;
import lombok.RequiredArgsConstructor;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Utils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
                Map<String, Object> payload = new HashMap<>();
                payload.put("title", title);
                payload.put("body", message);

                Notification notification = new Notification(
                    sub.getWebPushUrl(),
                    sub.getPublicKey(),
                    sub.getAuthKey(),
                    payload.toString().getBytes()
                );
                pushService.send(notification);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException(ErrorCode.WEB_PUSH_ERROR,
                    "subscriptionId: " + sub.getId() + ", " + e.getMessage());
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.WEB_PUSH_ERROR,
                    "subscriptionId: " + sub.getId() + ", " + e.getMessage());
            }
        }
    }
}
