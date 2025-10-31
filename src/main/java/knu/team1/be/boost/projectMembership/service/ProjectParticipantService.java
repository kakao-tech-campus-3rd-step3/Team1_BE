package knu.team1.be.boost.projectMembership.service;

import java.util.Optional;
import java.util.UUID;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.member.repository.MemberRepository;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.project.repository.ProjectRepository;
import knu.team1.be.boost.projectMembership.entity.ProjectMembership;
import knu.team1.be.boost.projectMembership.entity.ProjectRole;
import knu.team1.be.boost.projectMembership.repository.ProjectMembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class ProjectParticipantService {

    private final ProjectMembershipRepository projectMembershipRepository;
    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;

    void checkProjectExists(UUID projectId) {
        boolean exists = projectRepository.existsById(projectId);
        if (!exists) {
            throw new BusinessException(
                ErrorCode.PROJECT_NOT_FOUND,
                "projectId: " + projectId
            );
        }
    }

    void checkMemberExists(UUID memberId) {
        boolean exists = memberRepository.existsById(memberId);
        if (!exists) {
            throw new BusinessException(
                ErrorCode.MEMBER_NOT_FOUND,
                "memberId: " + memberId
            );
        }
    }

    @Transactional
    void joinProject(UUID projectId, UUID memberId, ProjectRole role) {

        // 프로젝트와 멤버 존재여부 확인
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PROJECT_NOT_FOUND,
                "projectId: " + projectId
            ));

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.MEMBER_NOT_FOUND,
                "memberId: " + memberId
            ));

        // 프로젝트와 멤버 관계 존재여부 확인
        Optional<ProjectMembership> existingRecord = projectMembershipRepository
            .findByProjectIdAndMemberIdIncludingDeleted(projectId, memberId);

        // 참여기록이 없다면 새로 생성해서 저장
        if (existingRecord.isEmpty()) {
            ProjectMembership newMembership = ProjectMembership.createProjectMembership(project,
                member, true, role);
            projectMembershipRepository.save(newMembership);
            return;
        }

        ProjectMembership projectMembership = existingRecord.get();

        // 참여했던 기록이 있으나 비활성화 상태면 재활성
        if (projectMembership.isDeleted()) {
            projectMembership.reactivate();
            projectMembership.updateRole(role);
            projectMembershipRepository.save(projectMembership);
            return;
        }

        // 이미 참여중이라면 예외처리
        throw new BusinessException(
            ErrorCode.MEMBER_ALREADY_JOINED,
            "projectId: " + projectId + ", memberId: " + memberId
        );
    }

    @Transactional
    void leaveProject(UUID projectId, UUID memberId) {
        ProjectMembership projectMembership = projectMembershipRepository
            .findByProjectIdAndMemberId(projectId, memberId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PROJECT_MEMBER_NOT_FOUND,
                "projectId: " + projectId + ", memberId: " + memberId
            ));

        // 프로젝트 소유자는 프로젝트를 나갈 수 없음
        if (projectMembership.getRole() == ProjectRole.OWNER) {
            throw new BusinessException(
                ErrorCode.PROJECT_OWNER_CANNOT_LEAVE,
                "projectId: " + projectId + ", memberId: " + memberId
            );
        }

        projectMembershipRepository.delete(projectMembership);
    }

}
