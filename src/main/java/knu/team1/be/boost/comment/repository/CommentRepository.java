package knu.team1.be.boost.comment.repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import knu.team1.be.boost.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findAllByTaskId(UUID taskId);

    @Query("SELECT c.task.id, COUNT(c) FROM Comment c WHERE c.task.id IN :taskIds AND c.deleted = false GROUP BY c.task.id")
    Map<UUID, Integer> countByTaskIds(@Param("taskIds") List<UUID> taskIds);
}
