package knu.team1.be.boost.projectMember.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectMemberServiceTest {

    @Captor
    ArgumentCaptor<ProjectMember> projectMemberCaptor;

    @Mock
    ProjectMemberRepository projectMemberRepository;
    @Mock
    ProjectRepository projectRepository;
    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    ProjectMemberService projectMemberService;

    @Nested
    @DisplayName("joinProject")
    class JoinProject {

        @Test
        @DisplayName("멤버가 프로젝트에 대한 참여 이력이 없을 때 join 성공")
        void join_first_success() {
            // given
            UUID projectId = randomProjectId();
            UUID memberId = randomMemberId();
            Project project = createTestProject();
            Member member = createTestMember();
            ProjectRole role = ProjectRole.MEMBER;

            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(projectMemberRepository.findByProjectIdAndMemberIdIncludingDeleted(projectId,
                memberId))
                .willReturn(Optional.empty());

            // when
            projectMemberService.joinProject(projectId, memberId, role);

            // then
            verify(projectMemberRepository).save(projectMemberCaptor.capture());
            ProjectMember saved = projectMemberCaptor.getValue();

            assertThat(saved.getProject()).isSameAs(project);
            assertThat(saved.getMember()).isSameAs(member);
            assertThat(saved.getRole()).isEqualTo(role);
        }

        @Test
        @DisplayName("멤버가 프로젝트에 대한 참여 이력이 있지만 현재 참여하고 있지 않을 때 join 성공")
        void join_not_first_success() {
            // given
            UUID projectId = randomProjectId();
            UUID memberId = randomMemberId();
            Project project = createTestProject();
            Member member = createTestMember();
            ProjectRole role = ProjectRole.MEMBER;
            ProjectMember projectMember = createDeletedProjectMember(project, member, role);

            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(projectMemberRepository.findByProjectIdAndMemberIdIncludingDeleted(
                projectId,
                memberId
            )).willReturn(Optional.of(projectMember));

            // when
            projectMemberService.joinProject(projectId, memberId, role);

            // then
            verify(projectMemberRepository).save(projectMemberCaptor.capture());
            ProjectMember saved = projectMemberCaptor.getValue();

            assertThat(saved.getProject()).isSameAs(project);
            assertThat(saved.getMember()).isSameAs(member);
            assertThat(saved.getRole()).isEqualTo(role);
        }

        @Test
        @DisplayName("이미 참여 중이면 MemberAlreadyJoinedException")
        void join_alreadyJoined() {
            // given
            UUID projectId = randomProjectId();
            UUID memberId = randomMemberId();
            Project project = createTestProject();
            Member member = createTestMember();
            ProjectRole role = ProjectRole.MEMBER;

            ProjectMember projectMember = ProjectMember.createProjectMember(project, member, role);
            // activeProjectMember는 기본적으로 deleted=false 상태

            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(projectMemberRepository.findByProjectIdAndMemberIdIncludingDeleted(
                projectId,
                memberId
            )).willReturn(Optional.of(projectMember));

            // when & then
            assertThatThrownBy(
                () -> projectMemberService.joinProject(projectId, memberId, role))
                .isInstanceOf(MemberAlreadyJoinedException.class);

            verify(projectMemberRepository, never()).save(any());
        }

        @Test
        @DisplayName("프로젝트가 없으면 ProjectNotFoundException")
        void join_projectNotFound() {
            // given
            UUID projectId = randomProjectId();
            UUID memberId = randomMemberId();
            ProjectRole role = ProjectRole.MEMBER;

            given(projectRepository.findById(projectId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                () -> projectMemberService.joinProject(projectId, memberId, role))
                .isInstanceOf(ProjectNotFoundException.class);

            verify(memberRepository, never()).findById(any());
            verify(projectMemberRepository, never()).save(any());
        }

        @Test
        @DisplayName("멤버가 없으면 MemberNotFoundException")
        void join_memberNotFound() {
            // given
            UUID projectId = randomProjectId();
            UUID memberId = randomMemberId();
            Project project = createTestProject();
            ProjectRole role = ProjectRole.MEMBER;

            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
            given(memberRepository.findById(memberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                () -> projectMemberService.joinProject(projectId, memberId, role))
                .isInstanceOf(MemberNotFoundException.class);
            
            verify(projectMemberRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("leaveProject")
    class LeaveProject {

        @Test
        @DisplayName("프로젝트에 참여하고 있는 멤버가 있으면 delete 호출")
        void leave_success() {
            // given
            UUID projectId = randomProjectId();
            UUID memberId = randomMemberId();
            Project project = createTestProject();
            Member member = createTestMember();
            ProjectRole role = ProjectRole.MEMBER;

            ProjectMember projectMember = createActiveProjectMember(project, member, role);

            given(projectMemberRepository.findByProjectIdAndMemberId(projectId, memberId))
                .willReturn(Optional.of(projectMember));

            // when
            projectMemberService.leaveProject(projectId, memberId);

            // then
            verify(projectMemberRepository).delete(projectMember);
        }

        @Test
        @DisplayName("프로젝트에 참여하고 있는 멤버가 없으면 ProjectMemberNotFoundException")
        void leave_notFound() {
            // given
            UUID projectId = randomProjectId();
            UUID memberId = randomMemberId();

            given(projectMemberRepository.findByProjectIdAndMemberId(projectId, memberId))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> projectMemberService.leaveProject(projectId, memberId))
                .isInstanceOf(ProjectMemberNotFoundException.class);

            verify(projectMemberRepository, never()).delete(any());
        }
    }

    // 테스트 데이터 생성 헬퍼 메서드들
    private UUID randomProjectId() {
        return UUID.randomUUID();
    }

    private UUID randomMemberId() {
        return UUID.randomUUID();
    }

    private Project createTestProject() {
        return Project.builder().build();
    }

    private Member createTestMember() {
        return Member.builder().build();
    }

    private ProjectMember createActiveProjectMember(
        Project project,
        Member member,
        ProjectRole role
    ) {
        return ProjectMember.createProjectMember(project, member, role);
    }

    private ProjectMember createDeletedProjectMember(
        Project project,
        Member member,
        ProjectRole role
    ) {
        return ProjectMember.builder()
            .project(project)
            .member(member)
            .role(role)
            .deleted(true)
            .deletedAt(LocalDateTime.now().minusDays(1))
            .build();
    }

}
