package knu.team1.be.boost.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoDto() {

    public record Token(
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_in") Integer expiresIn,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("refresh_token_expires_in") Integer refreshTokenExpiresIn,
        @JsonProperty("scope") String scope
    ) {

    }

    public record UserInfo(
        @JsonProperty("id") String id,
        @JsonProperty("kakao_account") KakaoAccount kakaoAccount
    ) {

        public record KakaoAccount(@JsonProperty("profile") Profile profile) {

        }

        public record Profile(@JsonProperty("nickname") String nickname) {

        }
    }
}
