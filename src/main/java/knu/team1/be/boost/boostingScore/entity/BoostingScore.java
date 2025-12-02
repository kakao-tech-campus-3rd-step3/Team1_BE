package knu.team1.be.boost.boostingScore.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import knu.team1.be.boost.common.entity.BaseEntity;
import knu.team1.be.boost.projectMembership.entity.ProjectMembership;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "boosting_scores")
public class BoostingScore extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_membership_id", nullable = false)
    private ProjectMembership projectMembership;

    @Column(name = "task_score", nullable = false)
    private Integer taskScore;

    @Column(name = "comment_score", nullable = false)
    private Integer commentScore;

    @Column(name = "approve_score", nullable = false)
    private Integer approveScore;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    public static BoostingScore create(
        ProjectMembership projectMembership,
        Integer taskScore,
        Integer commentScore,
        Integer approveScore,
        LocalDateTime calculatedAt
    ) {
        return BoostingScore.builder()
            .projectMembership(projectMembership)
            .taskScore(taskScore)
            .commentScore(commentScore)
            .approveScore(approveScore)
            .totalScore(taskScore + commentScore + approveScore)
            .calculatedAt(calculatedAt)
            .build();
    }
}

