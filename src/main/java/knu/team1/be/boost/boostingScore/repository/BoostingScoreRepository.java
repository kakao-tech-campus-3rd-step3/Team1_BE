package knu.team1.be.boost.boostingScore.repository;

import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.boostingScore.entity.BoostingScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoostingScoreRepository extends JpaRepository<BoostingScore, UUID> {

    /**
     * 특정 프로젝트의 최신 Boosting Score 목록을 조회합니다. 각 멤버별로 가장 최근에 계산된 점수만 반환합니다.
     */
    @Query("""
        SELECT bs
        FROM BoostingScore bs
        WHERE bs.projectMembership.project.id = :projectId
        AND bs.calculatedAt = (
            SELECT MAX(bs2.calculatedAt)
            FROM BoostingScore bs2
            WHERE bs2.projectMembership.id = bs.projectMembership.id
        )
        ORDER BY bs.totalScore DESC, bs.calculatedAt DESC
        """)
    List<BoostingScore> findLatestByProjectId(@Param("projectId") UUID projectId);

    /**
     * 특정 프로젝트에 대한 점수가 존재하는지 확인합니다.
     */
    @Query("""
        SELECT CASE WHEN COUNT(bs) > 0 THEN true ELSE false END
        FROM BoostingScore bs
        WHERE bs.projectMembership.project.id = :projectId
        """)
    boolean existsByProjectId(@Param("projectId") UUID projectId);
}

