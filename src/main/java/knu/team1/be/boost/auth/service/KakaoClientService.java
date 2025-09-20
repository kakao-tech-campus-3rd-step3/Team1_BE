package knu.team1.be.boost.auth.service;

import knu.team1.be.boost.auth.dto.KakaoDto;
import knu.team1.be.boost.auth.dto.KakaoDto.Token;
import knu.team1.be.boost.auth.exception.KakaoInvalidAuthCodeException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class KakaoClientService {

    private final WebClient webClient;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String kakaoTokenUri;

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String kakaoUserInfoUri;

    public KakaoDto.UserInfo getUserInfo(String code) {
        // 인가 코드로 액세스 토큰 요청
        Token tokenResponse = getToken(code);

        // 액세스 토큰으로 사용자 정보 요청
        return webClient.get()
            .uri(kakaoUserInfoUri)
            .header("Authorization", "Bearer " + tokenResponse.accessToken())
            .retrieve()
            .bodyToMono(KakaoDto.UserInfo.class)
            .block();
    }

    private Token getToken(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", kakaoClientId);
        formData.add("redirect_uri", kakaoRedirectUri);
        formData.add("code", code);

        return webClient.post()
            .uri(kakaoTokenUri)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(formData)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                Mono.error(new KakaoInvalidAuthCodeException())
            )
            .bodyToMono(Token.class)
            .block();
    }
}
