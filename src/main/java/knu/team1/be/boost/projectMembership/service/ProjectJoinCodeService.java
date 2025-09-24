package knu.team1.be.boost.projectMembership.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.project.repository.ProjectRepository;
import knu.team1.be.boost.projectMembership.entity.ProjectJoinCode;
import knu.team1.be.boost.projectMembership.repository.ProjectJoinCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class ProjectJoinCodeService {

    // Crockford Base32 Alphabet (혼동되는 글자 제외)
    private static final String CROCKFORD_BASE32 = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ProjectJoinCodeRepository projectJoinCodeRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    ProjectJoinCode generateJoinCode(UUID projectId) {

        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PROJECT_NOT_FOUND,
                "projectId: " + projectId
            ));

        projectJoinCodeRepository.findByProjectId(projectId)
            .ifPresent(ProjectJoinCode::revoke);

        String inviteCode;
        do {
            inviteCode = generateRandomCode();
        } while (projectJoinCodeRepository.existsByJoinCode(inviteCode));

        LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);

        ProjectJoinCode joinCode = ProjectJoinCode.create(project, inviteCode, expiresAt);
        return projectJoinCodeRepository.save(joinCode);
    }

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            int index = RANDOM.nextInt(CROCKFORD_BASE32.length());
            sb.append(CROCKFORD_BASE32.charAt(index));
        }
        return sb.toString();
    }
}

