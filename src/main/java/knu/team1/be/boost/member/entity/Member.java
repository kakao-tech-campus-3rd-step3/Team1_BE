package knu.team1.be.boost.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import knu.team1.be.boost.entity.BaseEntity;
import knu.team1.be.boost.member.vo.OauthInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@SuperBuilder
@Table(name = "members", uniqueConstraints = {
    @UniqueConstraint(
        name = "uk_member_provider_provider_id",
        columnNames = {"provider", "provider_id"}
    )
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE members SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Member extends BaseEntity {

    @Embedded
    private OauthInfo oauthInfo;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "profile_emoji", nullable = false)
    private String profileEmoji;

    public void updateMember(String name, String profileEmoji) {
        if (name != null) {
            this.name = name;
        }
        if (profileEmoji != null) {
            this.profileEmoji = profileEmoji;
        }
    }
}
