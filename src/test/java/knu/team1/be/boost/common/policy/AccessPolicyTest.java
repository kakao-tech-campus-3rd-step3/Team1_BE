package knu.team1.be.boost.common.policy;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Set;
import java.util.UUID;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.member.entity.vo.OauthInfo;
import knu.team1.be.boost.projectMembership.entity.ProjectRole;
import knu.team1.be.boost.projectMembership.repository.ProjectMembershipRepository;
import knu.team1.be.boost.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessPolicyTest {

    @Mock
    private ProjectMembershipRepository projectMembershipRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private AccessPolicy accessPolicy;

    private UUID projectId;
    private UUID memberId;
    private UUID taskId;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        taskId = UUID.randomUUID();
    }

    @Test
    @DisplayName("ensureProjectMember - 프로젝트 멤버일 때 통과")
    void ensureProjectMember_success() {
        given(projectMembershipRepository.existsByProjectIdAndMemberId(projectId, memberId))
            .willReturn(true);

        assertDoesNotThrow(() -> accessPolicy.ensureProjectMember(projectId, memberId));
    }

    @Test
    @DisplayName("ensureProjectMember - 프로젝트 멤버가 아닐 때 예외 발생")
    void ensureProjectMember_fail() {
        given(projectMembershipRepository.existsByProjectIdAndMemberId(projectId, memberId))
            .willReturn(false);

        assertThatThrownBy(() -> accessPolicy.ensureProjectMember(projectId, memberId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_MEMBER_ONLY);
    }

    @Test
    @DisplayName("ensureAssigneesAreProjectMembers - 모든 담당자가 프로젝트 멤버일 때 통과")
    void ensureAssignees_success() {
        Member member1 = Fixtures.member(UUID.randomUUID(), "Alice");
        Member member2 = Fixtures.member(UUID.randomUUID(), "Bob");
        Set<Member> assignees = Set.of(member1, member2);

        given(projectMembershipRepository.countByProjectIdAndMemberIdIn(
            projectId, Set.of(member1.getId(), member2.getId())))
            .willReturn(2);

        assertDoesNotThrow(
            () -> accessPolicy.ensureAssigneesAreProjectMembers(projectId, assignees));
    }

    @Test
    @DisplayName("ensureAssigneesAreProjectMembers - 일부 담당자가 프로젝트 멤버가 아닐 때 예외 발생")
    void ensureAssignees_fail() {
        Member member1 = Fixtures.member(UUID.randomUUID(), "Alice");
        Member member2 = Fixtures.member(UUID.randomUUID(), "Bob");
        Set<Member> assignees = Set.of(member1, member2);

        given(projectMembershipRepository.countByProjectIdAndMemberIdIn(
            projectId, Set.of(member1.getId(), member2.getId())))
            .willReturn(1);

        assertThatThrownBy(
            () -> accessPolicy.ensureAssigneesAreProjectMembers(projectId, assignees))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_MEMBER_ONLY);
    }

    @Test
    @DisplayName("ensureAssigneesAreProjectMembers - 담당자 목록이 null 또는 empty일 경우 바로 통과")
    void ensureAssignees_emptyOrNull() {
        assertDoesNotThrow(
            () -> accessPolicy.ensureAssigneesAreProjectMembers(projectId, null));
        assertDoesNotThrow(
            () -> accessPolicy.ensureAssigneesAreProjectMembers(projectId, Set.of()));
        verifyNoInteractions(projectMembershipRepository);
    }

    @Test
    @DisplayName("ensureTaskAssignee - 작업 담당자일 때 통과")
    void ensureTaskAssignee_success() {
        given(taskRepository.existsByIdAndAssigneesId(taskId, memberId))
            .willReturn(true);

        assertDoesNotThrow(() -> accessPolicy.ensureTaskAssignee(taskId, memberId));
    }

    @Test
    @DisplayName("ensureTaskAssignee - 작업 담당자가 아닐 때 예외 발생")
    void ensureTaskAssignee_fail() {
        given(taskRepository.existsByIdAndAssigneesId(taskId, memberId))
            .willReturn(false);

        assertThatThrownBy(() -> accessPolicy.ensureTaskAssignee(taskId, memberId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TASK_ASSIGNEE_ONLY);
    }

    @Test
    @DisplayName("ensureProjectOwner - 프로젝트 소유자일 때 통과")
    void ensureProjectOwner_success() {
        given(projectMembershipRepository.existsByProjectIdAndMemberIdAndRole(
            projectId, memberId, ProjectRole.OWNER))
            .willReturn(true);

        assertDoesNotThrow(() -> accessPolicy.ensureProjectOwner(projectId, memberId));
    }

    @Test
    @DisplayName("ensureProjectOwner - 프로젝트 소유자가 아닐 때 예외 발생")
    void ensureProjectOwner_fail() {
        given(projectMembershipRepository.existsByProjectIdAndMemberIdAndRole(
            projectId, memberId, ProjectRole.OWNER))
            .willReturn(false);

        assertThatThrownBy(() -> accessPolicy.ensureProjectOwner(projectId, memberId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_OWNER_ONLY);
    }

    @Test
    @DisplayName("ensureCommentAuthor - 댓글 작성자 본인일 때 통과")
    void ensureCommentAuthor_success() {
        assertDoesNotThrow(() -> accessPolicy.ensureCommentAuthor(memberId, memberId));
    }

    @Test
    @DisplayName("ensureCommentAuthor - 댓글 작성자가 아닐 때 예외 발생")
    void ensureCommentAuthor_fail() {
        UUID otherId = UUID.randomUUID();

        assertThatThrownBy(() -> accessPolicy.ensureCommentAuthor(memberId, otherId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_AUTHOR_ONLY);
    }

    static class Fixtures {

        static Member member(UUID id, String name) {
            return Member.builder()
                .id(id)
                .name(name)
                .avatar("https://example.com/avatar/" + name)
                .backgroundColor("#FFFFFF")
                .notificationEnabled(true)
                .oauthInfo(OauthInfo.builder()
                    .provider("google")
                    .providerId(123456789L)
                    .build())
                .build();
        }
    }

}
