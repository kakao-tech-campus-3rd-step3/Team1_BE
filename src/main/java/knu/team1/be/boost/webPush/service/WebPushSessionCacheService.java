package knu.team1.be.boost.webPush.service;

import knu.team1.be.boost.webPush.dto.WebPushSession;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class WebPushSessionCacheService {

    @CachePut(value = "webPushSessions", key = "#session.token()")
    public WebPushSession saveSession(WebPushSession session) {
        return session;
    }

    @Cacheable(value = "webPushSessions", key = "#token")
    public WebPushSession getSession(String token) {
        return null;
    }
}
