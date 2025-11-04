package knu.team1.be.boost.tag.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import knu.team1.be.boost.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    Optional<Tag> findByProjectIdAndName(UUID projectId, String name);

    List<Tag> findAllByProjectId(UUID projectId);

    @Query(
        value = """
                SELECT *
                FROM tags
                WHERE project_id = :projectId
                  AND LOWER(name) = LOWER(:name)
                LIMIT 1
            """,
        nativeQuery = true
    )
    Optional<Tag> findByProjectIdAndNameIncludingDeleted(UUID projectId, String name);

    @Modifying(clearAutomatically = true)
    @Query(
        "UPDATE Tag t SET t.deleted = true, t.deletedAt = CURRENT_TIMESTAMP WHERE t.project.id = :projectId"
    )
    void deleteAllByProjectId(UUID projectId);
}
