package knu.team1.be.boost.projectMembership.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.UUID;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.common.policy.AccessPolicy;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.projectMembership.dto.ProjectJoinCodeResponseDto;
import knu.team1.be.boost.projectMembership.dto.ProjectJoinRequestDto;
import knu.team1.be.boost.projectMembership.dto.ProjectJoinResponseDto;
import knu.team1.be.boost.projectMembership.entity.CodeStatus;
import knu.team1.be.boost.projectMembership.entity.ProjectJoinCode;
import knu.team1.be.boost.projectMembership.entity.ProjectRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectMembershipServiceTest {

    @InjectMocks
    private ProjectMembershipService projectMembershipService;

    @Mock
    private ProjectParticipantService projectParticipantService;

    @Mock
    private ProjectJoinCodeService projectJoinCodeService;

    @Mock
    private AccessPolicy accessPolicy;

    private final UUID projectId = UUID.randomUUID();
    private final UUID memberId = UUID.randomUUID();
    private final UUID targetMemberId = UUID.randomUUID();

    private Project testProject;
    private ProjectJoinCode testJoinCode;

    @BeforeEach
    void setUp() {
        testProject = Project.builder()
            .id(projectId)
            .name("테스트 프로젝트")
            .defaultReviewerCount(2)
            .build();

        testJoinCode = ProjectJoinCode.builder()
            .id(UUID.randomUUID())
            .project(testProject)
            .joinCode("A1B2C3")
            .status(CodeStatus.ACTIVE)
            .expiresAt(LocalDateTime.now().plusDays(7))
            .build();
    }

    @Test
    @DisplayName("프로젝트 참여 코드 생성 성공")
    void generateCode_Success() {
        // given
        doNothing().when(projectParticipantService).checkProjectExists(projectId);
        doNothing().when(projectParticipantService).checkMemberExists(memberId);
        doNothing().when(accessPolicy).ensureProjectMember(projectId, memberId);
        when(projectJoinCodeService.generateJoinCode(projectId)).thenReturn(testJoinCode);

        // when
        ProjectJoinCodeResponseDto result = projectMembershipService.generateCode(projectId,
            memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.joinCode()).isEqualTo("A1B2C3");
        assertThat(result.expiresAt()).isNotNull();

        verify(projectParticipantService, times(1)).checkProjectExists(projectId);
        verify(projectParticipantService, times(1)).checkMemberExists(memberId);
        verify(accessPolicy, times(1)).ensureProjectMember(projectId, memberId);
        verify(projectJoinCodeService, times(1)).generateJoinCode(projectId);
    }

    @Test
    @DisplayName("프로젝트 참여 코드 생성 실패 - 프로젝트 없음")
    void generateCode_Fail_ProjectNotFound() {
        // given
        doThrow(new BusinessException(ErrorCode.PROJECT_NOT_FOUND))
            .when(projectParticipantService).checkProjectExists(projectId);

        // when & then
        assertThatThrownBy(() -> projectMembershipService.generateCode(projectId, memberId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);

        verify(projectParticipantService, times(1)).checkProjectExists(projectId);
        verify(projectJoinCodeService, never()).generateJoinCode(any());
    }

    @Test
    @DisplayName("프로젝트 참여 코드 생성 실패 - 권한 없음")
    void generateCode_Fail_NoPermission() {
        // given
        doNothing().when(projectParticipantService).checkProjectExists(projectId);
        doNothing().when(projectParticipantService).checkMemberExists(memberId);
        doThrow(new BusinessException(ErrorCode.PROJECT_MEMBER_ONLY))
            .when(accessPolicy).ensureProjectMember(projectId, memberId);

        // when & then
        assertThatThrownBy(() -> projectMembershipService.generateCode(projectId, memberId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_MEMBER_ONLY);

        verify(accessPolicy, times(1)).ensureProjectMember(projectId, memberId);
        verify(projectJoinCodeService, never()).generateJoinCode(any());
    }

    @Test
    @DisplayName("프로젝트 참여 코드 조회 성공")
    void getCode_Success() {
        // given
        doNothing().when(projectParticipantService).checkProjectExists(projectId);
        doNothing().when(projectParticipantService).checkMemberExists(memberId);
        doNothing().when(accessPolicy).ensureProjectMember(projectId, memberId);
        when(projectJoinCodeService.getJoinCode(projectId)).thenReturn(testJoinCode);

        // when
        ProjectJoinCodeResponseDto result = projectMembershipService.getCode(projectId, memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.joinCode()).isEqualTo("A1B2C3");
        assertThat(result.expiresAt()).isNotNull();

        verify(projectParticipantService, times(1)).checkProjectExists(projectId);
        verify(projectParticipantService, times(1)).checkMemberExists(memberId);
        verify(accessPolicy, times(1)).ensureProjectMember(projectId, memberId);
        verify(projectJoinCodeService, times(1)).getJoinCode(projectId);
    }

    @Test
    @DisplayName("프로젝트 참여 코드 조회 실패 - 권한 없음")
    void getCode_Fail_NoPermission() {
        // given
        doNothing().when(projectParticipantService).checkProjectExists(projectId);
        doNothing().when(projectParticipantService).checkMemberExists(memberId);
        doThrow(new BusinessException(ErrorCode.PROJECT_MEMBER_ONLY))
            .when(accessPolicy).ensureProjectMember(projectId, memberId);

        // when & then
        assertThatThrownBy(() -> projectMembershipService.getCode(projectId, memberId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_MEMBER_ONLY);

        verify(accessPolicy, times(1)).ensureProjectMember(projectId, memberId);
        verify(projectJoinCodeService, never()).getJoinCode(any());
    }

    @Test
    @DisplayName("프로젝트 참여 성공")
    void joinProject_Success() {
        // given
        ProjectJoinRequestDto requestDto = new ProjectJoinRequestDto("A1B2C3");

        doNothing().when(projectParticipantService).checkMemberExists(memberId);
        when(projectJoinCodeService.validateJoinCode("A1B2C3")).thenReturn(testJoinCode);
        doNothing().when(projectParticipantService)
            .joinProject(projectId, memberId, ProjectRole.MEMBER);

        // when
        ProjectJoinResponseDto result = projectMembershipService.joinProject(requestDto, memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.projectId()).isEqualTo(projectId);

        verify(projectParticipantService, times(1)).checkMemberExists(memberId);
        verify(projectJoinCodeService, times(1)).validateJoinCode("A1B2C3");
        verify(projectParticipantService, times(1))
            .joinProject(projectId, memberId, ProjectRole.MEMBER);
    }

    @Test
    @DisplayName("프로젝트 참여 실패 - 유효하지 않은 코드")
    void joinProject_Fail_InvalidCode() {
        // given
        ProjectJoinRequestDto requestDto = new ProjectJoinRequestDto("XXXXXX");

        doNothing().when(projectParticipantService).checkMemberExists(memberId);
        when(projectJoinCodeService.validateJoinCode("XXXXXX"))
            .thenThrow(new BusinessException(ErrorCode.JOIN_CODE_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> projectMembershipService.joinProject(requestDto, memberId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.JOIN_CODE_NOT_FOUND);

        verify(projectJoinCodeService, times(1)).validateJoinCode("XXXXXX");
        verify(projectParticipantService, never()).joinProject(any(), any(), any());
    }

    @Test
    @DisplayName("프로젝트 참여 실패 - 이미 참여 중")
    void joinProject_Fail_AlreadyMember() {
        // given
        ProjectJoinRequestDto requestDto = new ProjectJoinRequestDto("A1B2C3");

        doNothing().when(projectParticipantService).checkMemberExists(memberId);
        when(projectJoinCodeService.validateJoinCode("A1B2C3")).thenReturn(testJoinCode);
        doThrow(new BusinessException(ErrorCode.MEMBER_ALREADY_JOINED))
            .when(projectParticipantService)
            .joinProject(projectId, memberId, ProjectRole.MEMBER);

        // when & then
        assertThatThrownBy(() -> projectMembershipService.joinProject(requestDto, memberId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_ALREADY_JOINED);

        verify(projectParticipantService, times(1))
            .joinProject(projectId, memberId, ProjectRole.MEMBER);
    }

    @Test
    @DisplayName("프로젝트 나가기 성공")
    void leaveProject_Success() {
        // given
        doNothing().when(projectParticipantService).checkMemberExists(memberId);
        doNothing().when(accessPolicy).ensureProjectMember(projectId, memberId);
        doNothing().when(projectParticipantService).leaveProject(projectId, memberId);

        // when
        projectMembershipService.leaveProject(projectId, memberId);

        // then
        verify(projectParticipantService, times(1)).checkMemberExists(memberId);
        verify(accessPolicy, times(1)).ensureProjectMember(projectId, memberId);
        verify(projectParticipantService, times(1)).leaveProject(projectId, memberId);
    }

    @Test
    @DisplayName("프로젝트 나가기 실패 - Owner는 나갈 수 없음")
    void leaveProject_Fail_OwnerCannotLeave() {
        // given
        doNothing().when(projectParticipantService).checkMemberExists(memberId);
        doNothing().when(accessPolicy).ensureProjectMember(projectId, memberId);
        doThrow(new BusinessException(ErrorCode.PROJECT_OWNER_CANNOT_LEAVE))
            .when(projectParticipantService).leaveProject(projectId, memberId);

        // when & then
        assertThatThrownBy(() -> projectMembershipService.leaveProject(projectId, memberId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_OWNER_CANNOT_LEAVE);

        verify(projectParticipantService, times(1)).leaveProject(projectId, memberId);
    }

    @Test
    @DisplayName("멤버 추방 성공")
    void kickMember_Success() {
        // given
        doNothing().when(projectParticipantService).checkProjectExists(projectId);
        doNothing().when(projectParticipantService).checkMemberExists(memberId);
        doNothing().when(projectParticipantService).checkMemberExists(targetMemberId);
        doNothing().when(accessPolicy).ensureProjectOwner(projectId, memberId);
        doNothing().when(projectParticipantService)
            .kickMember(projectId, targetMemberId, memberId);

        // when
        projectMembershipService.kickMember(projectId, targetMemberId, memberId);

        // then
        verify(projectParticipantService, times(1)).checkProjectExists(projectId);
        verify(projectParticipantService, times(1)).checkMemberExists(memberId);
        verify(projectParticipantService, times(1)).checkMemberExists(targetMemberId);
        verify(accessPolicy, times(1)).ensureProjectOwner(projectId, memberId);
        verify(projectParticipantService, times(1))
            .kickMember(projectId, targetMemberId, memberId);
    }

    @Test
    @DisplayName("멤버 추방 실패 - 권한 없음 (Owner 아님)")
    void kickMember_Fail_NotOwner() {
        // given
        doNothing().when(projectParticipantService).checkProjectExists(projectId);
        doNothing().when(projectParticipantService).checkMemberExists(memberId);
        doNothing().when(projectParticipantService).checkMemberExists(targetMemberId);
        doThrow(new BusinessException(ErrorCode.PROJECT_OWNER_ONLY))
            .when(accessPolicy).ensureProjectOwner(projectId, memberId);

        // when & then
        assertThatThrownBy(
            () -> projectMembershipService.kickMember(projectId, targetMemberId, memberId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_OWNER_ONLY);

        verify(accessPolicy, times(1)).ensureProjectOwner(projectId, memberId);
        verify(projectParticipantService, never()).kickMember(any(), any(), any());
    }

    @Test
    @DisplayName("멤버 추방 실패 - 자기 자신 추방 시도")
    void kickMember_Fail_CannotKickSelf() {
        // given
        doNothing().when(projectParticipantService).checkProjectExists(projectId);
        doNothing().when(projectParticipantService).checkMemberExists(memberId);
        doNothing().when(accessPolicy).ensureProjectOwner(projectId, memberId);
        doThrow(new BusinessException(ErrorCode.CANNOT_KICK_YOURSELF))
            .when(projectParticipantService).kickMember(projectId, memberId, memberId);

        // when & then
        assertThatThrownBy(
            () -> projectMembershipService.kickMember(projectId, memberId, memberId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CANNOT_KICK_YOURSELF);

        verify(projectParticipantService, times(1)).kickMember(projectId, memberId, memberId);
    }

    @Test
    @DisplayName("멤버 추방 실패 - Owner 추방 시도")
    void kickMember_Fail_CannotKickOwner() {
        // given
        doNothing().when(projectParticipantService).checkProjectExists(projectId);
        doNothing().when(projectParticipantService).checkMemberExists(memberId);
        doNothing().when(projectParticipantService).checkMemberExists(targetMemberId);
        doNothing().when(accessPolicy).ensureProjectOwner(projectId, memberId);
        doThrow(new BusinessException(ErrorCode.CANNOT_KICK_OWNER))
            .when(projectParticipantService).kickMember(projectId, targetMemberId, memberId);

        // when & then
        assertThatThrownBy(
            () -> projectMembershipService.kickMember(projectId, targetMemberId, memberId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CANNOT_KICK_OWNER);

        verify(projectParticipantService, times(1))
            .kickMember(projectId, targetMemberId, memberId);
    }
}

