package knu.team1.be.boost.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.common.policy.AccessPolicy;
import knu.team1.be.boost.member.dto.MemberResponseDto;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.member.repository.MemberRepository;
import knu.team1.be.boost.project.dto.ProjectCreateRequestDto;
import knu.team1.be.boost.project.dto.ProjectResponseDto;
import knu.team1.be.boost.project.dto.ProjectUpdateRequestDto;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.project.repository.ProjectRepository;
import knu.team1.be.boost.projectMembership.entity.ProjectMembership;
import knu.team1.be.boost.projectMembership.entity.ProjectRole;
import knu.team1.be.boost.projectMembership.repository.ProjectMembershipRepository;
import knu.team1.be.boost.tag.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @InjectMocks
    private ProjectService projectService;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ProjectMembershipRepository projectMembershipRepository;

    @Mock
    private AccessPolicy accessPolicy;

    private final UUID projectId = UUID.randomUUID();
    private final UUID memberId = UUID.randomUUID();

    private Project testProject;
    private Member testMember;
    private ProjectMembership testMembership;

    @BeforeEach
    void setUp() {
        testProject = Project.builder()
            .id(projectId)
            .name("테스트 프로젝트")
            .defaultReviewerCount(2)
            .build();

        testMember = Member.builder()
            .id(memberId)
            .name("테스트유저")
            .avatar("1111")
            .backgroundColor("#FF5733")
            .build();

        testMembership = ProjectMembership.builder()
            .id(UUID.randomUUID())
            .project(testProject)
            .member(testMember)
            .role(ProjectRole.OWNER)
            .notificationEnabled(true)
            .build();
    }

    @Test
    @DisplayName("프로젝트 생성 성공")
    void createProject_Success() {
        // given
        ProjectCreateRequestDto requestDto = new ProjectCreateRequestDto("새 프로젝트");

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);
        when(projectMembershipRepository.save(any(ProjectMembership.class)))
            .thenReturn(testMembership);

        // when
        ProjectResponseDto result = projectService.createProject(requestDto, memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(projectId);
        assertThat(result.name()).isEqualTo("테스트 프로젝트");
        assertThat(result.defaultReviewerCount()).isEqualTo(2);
        assertThat(result.role()).isEqualTo(ProjectRole.OWNER);
        assertThat(result.isNotificationEnabled()).isTrue();

        verify(memberRepository, times(1)).findById(memberId);
        verify(projectRepository, times(1)).save(any(Project.class));
        verify(projectMembershipRepository, times(1)).save(any(ProjectMembership.class));
    }

    @Test
    @DisplayName("프로젝트 생성 실패 - 멤버 없음")
    void createProject_Fail_MemberNotFound() {
        // given
        ProjectCreateRequestDto requestDto = new ProjectCreateRequestDto("새 프로젝트");

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectService.createProject(requestDto, memberId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);

        verify(memberRepository, times(1)).findById(memberId);
        verify(projectRepository, never()).save(any());
        verify(projectMembershipRepository, never()).save(any());
    }

    @Test
    @DisplayName("프로젝트 조회 성공")
    void getProject_Success() {
        // given
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        doNothing().when(accessPolicy).ensureProjectMember(projectId, memberId);
        when(projectMembershipRepository.findByProjectIdAndMemberId(projectId, memberId))
            .thenReturn(Optional.of(testMembership));

        // when
        ProjectResponseDto result = projectService.getProject(projectId, memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(projectId);
        assertThat(result.name()).isEqualTo("테스트 프로젝트");
        assertThat(result.role()).isEqualTo(ProjectRole.OWNER);

        verify(projectRepository, times(1)).findById(projectId);
        verify(accessPolicy, times(1)).ensureProjectMember(projectId, memberId);
        verify(projectMembershipRepository, times(1))
            .findByProjectIdAndMemberId(projectId, memberId);
    }

    @Test
    @DisplayName("프로젝트 조회 실패 - 프로젝트 없음")
    void getProject_Fail_ProjectNotFound() {
        // given
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectService.getProject(projectId, memberId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);

        verify(projectRepository, times(1)).findById(projectId);
        verify(accessPolicy, never()).ensureProjectMember(any(), any());
    }

    @Test
    @DisplayName("프로젝트 조회 실패 - 권한 없음")
    void getProject_Fail_NoPermission() {
        // given
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        doThrow(new BusinessException(ErrorCode.PROJECT_MEMBER_ONLY))
            .when(accessPolicy).ensureProjectMember(projectId, memberId);

        // when & then
        assertThatThrownBy(() -> projectService.getProject(projectId, memberId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_MEMBER_ONLY);

        verify(projectRepository, times(1)).findById(projectId);
        verify(accessPolicy, times(1)).ensureProjectMember(projectId, memberId);
        verify(projectMembershipRepository, never()).findByProjectIdAndMemberId(any(), any());
    }

    @Test
    @DisplayName("내 프로젝트 목록 조회 성공")
    void getMyProjects_Success() {
        // given
        ProjectMembership membership2 = ProjectMembership.builder()
            .id(UUID.randomUUID())
            .project(Project.builder().id(UUID.randomUUID()).name("프로젝트2").defaultReviewerCount(3).build())
            .member(testMember)
            .role(ProjectRole.MEMBER)
            .notificationEnabled(false)
            .build();

        when(projectMembershipRepository.findAllByMemberId(memberId))
            .thenReturn(List.of(testMembership, membership2));

        // when
        List<ProjectResponseDto> result = projectService.getMyProjects(memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).name()).isEqualTo("테스트 프로젝트");
        assertThat(result.get(0).role()).isEqualTo(ProjectRole.OWNER);
        assertThat(result.get(1).name()).isEqualTo("프로젝트2");
        assertThat(result.get(1).role()).isEqualTo(ProjectRole.MEMBER);

        verify(projectMembershipRepository, times(1)).findAllByMemberId(memberId);
    }

    @Test
    @DisplayName("내 프로젝트 목록 조회 - 빈 리스트")
    void getMyProjects_EmptyList() {
        // given
        when(projectMembershipRepository.findAllByMemberId(memberId))
            .thenReturn(List.of());

        // when
        List<ProjectResponseDto> result = projectService.getMyProjects(memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(projectMembershipRepository, times(1)).findAllByMemberId(memberId);
    }

    @Test
    @DisplayName("프로젝트 수정 성공")
    void updateProject_Success() {
        // given
        ProjectUpdateRequestDto requestDto = new ProjectUpdateRequestDto("수정된 프로젝트", 3);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        doNothing().when(accessPolicy).ensureProjectOwner(projectId, memberId);
        when(projectMembershipRepository.findByProjectIdAndMemberId(projectId, memberId))
            .thenReturn(Optional.of(testMembership));

        // when
        ProjectResponseDto result = projectService.updateProject(projectId, requestDto, memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(projectId);
        assertThat(result.role()).isEqualTo(ProjectRole.OWNER);

        verify(projectRepository, times(1)).findById(projectId);
        verify(accessPolicy, times(1)).ensureProjectOwner(projectId, memberId);
        verify(projectMembershipRepository, times(1))
            .findByProjectIdAndMemberId(projectId, memberId);
    }

    @Test
    @DisplayName("프로젝트 수정 실패 - 프로젝트 없음")
    void updateProject_Fail_ProjectNotFound() {
        // given
        ProjectUpdateRequestDto requestDto = new ProjectUpdateRequestDto("수정된 프로젝트", 3);

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectService.updateProject(projectId, requestDto, memberId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);

        verify(projectRepository, times(1)).findById(projectId);
        verify(accessPolicy, never()).ensureProjectOwner(any(), any());
    }

    @Test
    @DisplayName("프로젝트 수정 실패 - 권한 없음 (Owner 아님)")
    void updateProject_Fail_NotOwner() {
        // given
        ProjectUpdateRequestDto requestDto = new ProjectUpdateRequestDto("수정된 프로젝트", 3);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        doThrow(new BusinessException(ErrorCode.PROJECT_OWNER_ONLY))
            .when(accessPolicy).ensureProjectOwner(projectId, memberId);

        // when & then
        assertThatThrownBy(() -> projectService.updateProject(projectId, requestDto, memberId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_OWNER_ONLY);

        verify(projectRepository, times(1)).findById(projectId);
        verify(accessPolicy, times(1)).ensureProjectOwner(projectId, memberId);
        verify(projectMembershipRepository, never()).findByProjectIdAndMemberId(any(), any());
    }

    @Test
    @DisplayName("프로젝트 삭제 성공")
    void deleteProject_Success() {
        // given
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        doNothing().when(accessPolicy).ensureProjectOwner(projectId, memberId);
        doNothing().when(tagRepository).deleteAllByProjectId(projectId);
        doNothing().when(projectMembershipRepository).softDeleteAllByProjectId(projectId);
        doNothing().when(projectRepository).delete(testProject);

        // when
        projectService.deleteProject(projectId, memberId);

        // then
        verify(projectRepository, times(1)).findById(projectId);
        verify(accessPolicy, times(1)).ensureProjectOwner(projectId, memberId);
        verify(tagRepository, times(1)).deleteAllByProjectId(projectId);
        verify(projectMembershipRepository, times(1)).softDeleteAllByProjectId(projectId);
        verify(projectRepository, times(1)).delete(testProject);
    }

    @Test
    @DisplayName("프로젝트 삭제 실패 - 프로젝트 없음")
    void deleteProject_Fail_ProjectNotFound() {
        // given
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectService.deleteProject(projectId, memberId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);

        verify(projectRepository, times(1)).findById(projectId);
        verify(accessPolicy, never()).ensureProjectOwner(any(), any());
        verify(projectRepository, never()).delete(any());
    }

    @Test
    @DisplayName("프로젝트 삭제 실패 - 권한 없음")
    void deleteProject_Fail_NotOwner() {
        // given
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        doThrow(new BusinessException(ErrorCode.PROJECT_OWNER_ONLY))
            .when(accessPolicy).ensureProjectOwner(projectId, memberId);

        // when & then
        assertThatThrownBy(() -> projectService.deleteProject(projectId, memberId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_OWNER_ONLY);

        verify(projectRepository, times(1)).findById(projectId);
        verify(accessPolicy, times(1)).ensureProjectOwner(projectId, memberId);
        verify(projectRepository, never()).delete(any());
    }

    @Test
    @DisplayName("프로젝트 멤버 목록 조회 성공")
    void getProjectMembers_Success() {
        // given
        Member member2 = Member.builder()
            .id(UUID.randomUUID())
            .name("멤버2")
            .avatar("2222")
            .backgroundColor("#0000FF")
            .build();

        ProjectMembership membership2 = ProjectMembership.builder()
            .id(UUID.randomUUID())
            .project(testProject)
            .member(member2)
            .role(ProjectRole.MEMBER)
            .notificationEnabled(true)
            .build();

        when(projectRepository.existsById(projectId)).thenReturn(true);
        doNothing().when(accessPolicy).ensureProjectMember(projectId, memberId);
        when(projectMembershipRepository.findAllByProjectId(projectId))
            .thenReturn(List.of(testMembership, membership2));

        // when
        List<MemberResponseDto> result = projectService.getProjectMembers(projectId, memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).name()).isEqualTo("테스트유저");
        assertThat(result.get(1).name()).isEqualTo("멤버2");

        verify(projectRepository, times(1)).existsById(projectId);
        verify(accessPolicy, times(1)).ensureProjectMember(projectId, memberId);
        verify(projectMembershipRepository, times(1)).findAllByProjectId(projectId);
    }

    @Test
    @DisplayName("프로젝트 멤버 목록 조회 실패 - 프로젝트 없음")
    void getProjectMembers_Fail_ProjectNotFound() {
        // given
        when(projectRepository.existsById(projectId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> projectService.getProjectMembers(projectId, memberId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);

        verify(projectRepository, times(1)).existsById(projectId);
        verify(accessPolicy, never()).ensureProjectMember(any(), any());
    }

    @Test
    @DisplayName("프로젝트 멤버 목록 조회 실패 - 권한 없음")
    void getProjectMembers_Fail_NoPermission() {
        // given
        when(projectRepository.existsById(projectId)).thenReturn(true);
        doThrow(new BusinessException(ErrorCode.PROJECT_MEMBER_ONLY))
            .when(accessPolicy).ensureProjectMember(projectId, memberId);

        // when & then
        assertThatThrownBy(() -> projectService.getProjectMembers(projectId, memberId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_MEMBER_ONLY);

        verify(projectRepository, times(1)).existsById(projectId);
        verify(accessPolicy, times(1)).ensureProjectMember(projectId, memberId);
        verify(projectMembershipRepository, never()).findAllByProjectId(any());
    }
}

