package knu.team1.be.boost.file.repository;

import java.util.UUID;
import knu.team1.be.boost.file.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRespository extends JpaRepository<File, UUID> {

}
