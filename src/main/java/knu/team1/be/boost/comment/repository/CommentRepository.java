package knu.team1.be.boost.comment.repository;

import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findAllByTaskId(UUID taskId);

    @Query("""
            SELECT c.task.id AS taskId, COUNT(c) AS count
            FROM Comment c
            WHERE c.task.id IN :taskIds AND c.deleted = false
            GROUP BY c.task.id
        """)
    List<CommentCount> countByTaskIds(@Param("taskIds") List<UUID> taskIds);

    interface CommentCount {

        UUID getTaskId();

        Long getCount();
    }
}
