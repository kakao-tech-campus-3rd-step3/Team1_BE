package knu.team1.be.boost.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import knu.team1.be.boost.entity.BaseEntity;
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

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "provider_id", unique = true, nullable = false)
    private String providerId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "profile_emoji", nullable = false)
    private String profileEmoji;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;
}
