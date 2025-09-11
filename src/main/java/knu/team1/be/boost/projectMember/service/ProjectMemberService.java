package knu.team1.be.boost.projectMember.service;

import java.util.Optional;
import java.util.UUID;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.member.exception.MemberNotFoundException;
import knu.team1.be.boost.member.repository.MemberRepository;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.project.exception.ProjectNotFoundException;
import knu.team1.be.boost.project.repository.ProjectRepository;
import knu.team1.be.boost.projectMember.entity.ProjectMember;
import knu.team1.be.boost.projectMember.entity.ProjectRole;
import knu.team1.be.boost.projectMember.exception.MemberAlreadyJoinedException;
import knu.team1.be.boost.projectMember.exception.ProjectMemberNotFoundException;
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
            .orElseThrow(() -> new ProjectNotFoundException(projectId));
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(memberId));

        // 이미 참여 중인지 확인
        Optional<ProjectMember> existing = projectMemberRepository
            .findByProjectIdAndMemberId(projectId, memberId);
        if (existing.isPresent()) {
            throw new MemberAlreadyJoinedException();
        }

        // 새로운 관계 생성
        ProjectMember projectMember = ProjectMember.createProjectMember(project, member, role);

        projectMemberRepository.save(projectMember);
    }

    @Transactional
    public void leaveProject(UUID projectId, UUID memberId) {
        ProjectMember projectMember = projectMemberRepository
            .findByProjectIdAndMemberId(projectId, memberId)
            .orElseThrow(() -> new ProjectMemberNotFoundException(projectId, memberId));

        projectMemberRepository.delete(projectMember);
    }


}
