package knu.team1.be.boost.member.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class OauthInfo {

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "provider_id", unique = true, nullable = false)
    private String providerId;

    public OauthInfo(String provider, String providerId) {
        this.provider = provider;
        this.providerId = providerId;
    }
}
