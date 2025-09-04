package knu.team1.be.boost.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import knu.team1.be.boost.entity.BaseEntity;
import knu.team1.be.boost.user.vo.OauthInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@SuperBuilder
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE users SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class User extends BaseEntity {

    @Embedded
    private OauthInfo oauthInfo;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "profile_emoji", nullable = false)
    private String profileEmoji;

    public void updateUser(String name, String profileEmoji) {
        this.name = name;
        this.profileEmoji = profileEmoji;
    }
}
