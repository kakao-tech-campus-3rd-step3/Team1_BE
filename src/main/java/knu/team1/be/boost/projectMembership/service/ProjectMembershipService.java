package knu.team1.be.boost.projectMembership.service;

import java.util.UUID;
import knu.team1.be.boost.common.policy.AccessPolicy;
import knu.team1.be.boost.projectMembership.dto.ProjectJoinCodeResponseDto;
import knu.team1.be.boost.projectMembership.entity.ProjectJoinCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectMembershipService {

    private final ProjectParticipantService projectParticipantService;
    private final ProjectJoinCodeService projectJoinCodeService;
    private final AccessPolicy accessPolicy;

    @Transactional
    public ProjectJoinCodeResponseDto generateCode(UUID projectId, UUID memberId) {

        projectParticipantService.checkProjectExists(projectId);
        projectParticipantService.checkMemberExists(memberId);

        accessPolicy.ensureProjectMember(projectId, memberId);

        ProjectJoinCode projectJoinCode = projectJoinCodeService.generateJoinCode(projectId);
        return ProjectJoinCodeResponseDto.from(projectJoinCode);
    }

    public ProjectJoinCodeResponseDto getCode(UUID projectId, UUID memberId) {

        projectParticipantService.checkProjectExists(projectId);
        projectParticipantService.checkMemberExists(memberId);

        accessPolicy.ensureProjectMember(projectId, memberId);

        ProjectJoinCode projectJoinCode = projectJoinCodeService.getJoinCode(projectId);
        return ProjectJoinCodeResponseDto.from(projectJoinCode);
    }
}

