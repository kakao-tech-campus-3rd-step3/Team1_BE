package knu.team1.be.boost.user.repository;

import java.util.UUID;
import knu.team1.be.boost.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

}
