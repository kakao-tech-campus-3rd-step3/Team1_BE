package knu.team1.be.boost.projectMember.service;

import java.util.Optional;
import java.util.UUID;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.member.repository.MemberRepository;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.project.repository.ProjectRepository;
import knu.team1.be.boost.projectMember.entity.ProjectMember;
import knu.team1.be.boost.projectMember.entity.ProjectRole;
import knu.team1.be.boost.projectMember.repository.ProjectMemberRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void joinProject(UUID projectId, UUID memberId, ProjectRole role) {

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
        Optional<ProjectMember> existingRecord = projectMemberRepository
            .findByProjectIdAndMemberIdIncludingDeleted(projectId, memberId);

        // 참여기록이 없다면 새로 생성해서 저장
        if (existingRecord.isEmpty()) {
            ProjectMember newMember = ProjectMember.createProjectMember(project, member, role);
            projectMemberRepository.save(newMember);
            return;
        }

        ProjectMember projectMember = existingRecord.get();

        // 참여했던 기록이 있으나 비활성화 상태면 재활성
        if (projectMember.isDeleted()) {
            projectMember.reactivate();
            projectMember.updateRole(role);
            projectMemberRepository.save(projectMember);
            return;
        }

        // 이미 참여중이라면 예외처리
        throw new BusinessException(
            ErrorCode.MEMBER_ALREADY_JOINED,
            "projectId: " + projectId + ", memberId: " + memberId
        );
    }

    @Transactional
    public void leaveProject(UUID projectId, UUID memberId) {
        ProjectMember projectMember = projectMemberRepository
            .findByProjectIdAndMemberId(projectId, memberId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PROJECT_MEMBER_NOT_FOUND,
                "projectId: " + projectId + ", memberId: " + memberId
            ));

        projectMemberRepository.delete(projectMember);
    }


}
