package knu.team1.be.boost.memo.service;

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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.common.policy.AccessPolicy;
import knu.team1.be.boost.memo.dto.MemoCreateRequestDto;
import knu.team1.be.boost.memo.dto.MemoItemResponseDto;
import knu.team1.be.boost.memo.dto.MemoResponseDto;
import knu.team1.be.boost.memo.dto.MemoUpdateRequestDto;
import knu.team1.be.boost.memo.entity.Memo;
import knu.team1.be.boost.memo.repository.MemoRepository;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.project.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemoServiceTest {

    @InjectMocks
    private MemoService memoService;

    @Mock
    private MemoRepository memoRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private AccessPolicy accessPolicy;

    private final UUID projectId = UUID.randomUUID();
    private final UUID memberId = UUID.randomUUID();
    private final UUID memoId = UUID.randomUUID();

    private Project testProject;
    private Memo testMemo;

    @BeforeEach
    void setUp() {
        testProject = Project.builder()
            .id(projectId)
            .build();

        testMemo = Memo.builder()
            .id(memoId)
            .project(testProject) // 이 메모는 testProject에 속함
            .title("테스트 메모")
            .content("테스트 내용")
            .createdAt(LocalDateTime.now().minusDays(1))
            .updatedAt(LocalDateTime.now().minusDays(1))
            .build();
    }

    @Test
    @DisplayName("createMemo: 메모 생성 성공")
    void createMemo_Success() {
        // given
        MemoCreateRequestDto requestDto = new MemoCreateRequestDto("새 메모", "새 내용");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        doNothing().when(accessPolicy).ensureProjectMember(projectId, memberId);
        when(memoRepository.save(any(Memo.class))).thenReturn(testMemo); // 편의상 testMemo 반환

        // when
        MemoResponseDto result = memoService.createMemo(projectId, memberId, requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("테스트 메모");

        verify(projectRepository, times(1)).findById(projectId);
        verify(accessPolicy, times(1)).ensureProjectMember(projectId, memberId);
        verify(memoRepository, times(1)).save(any(Memo.class));
    }

    @Test
    @DisplayName("createMemo: 메모 생성 실패 - 프로젝트 없음")
    void createMemo_Fail_ProjectNotFound() {
        // given
        MemoCreateRequestDto requestDto = new MemoCreateRequestDto("새 메모", "새 내용");

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memoService.createMemo(projectId, memberId, requestDto))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);

        verify(accessPolicy, never()).ensureProjectMember(any(), any());
        verify(memoRepository, never()).save(any());
    }

    @Test
    @DisplayName("createMemo: 메모 생성 실패 - 접근 권한 없음")
    void createMemo_Fail_AccessDenied() {
        // given
        MemoCreateRequestDto requestDto = new MemoCreateRequestDto("새 메모", "새 내용");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        doThrow(new BusinessException(ErrorCode.PROJECT_MEMBER_ONLY, "권한 없음"))
            .when(accessPolicy).ensureProjectMember(projectId, memberId);

        // when & then
        assertThatThrownBy(() -> memoService.createMemo(projectId, memberId, requestDto))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_MEMBER_ONLY);

        verify(memoRepository, never()).save(any());
    }

    @Test
    @DisplayName("findMemosByProjectId: 메모 목록 조회 성공")
    void findMemosByProjectId_Success() {
        // given
        when(projectRepository.existsById(projectId)).thenReturn(true);
        doNothing().when(accessPolicy).ensureProjectMember(projectId, memberId);
        when(memoRepository.findAllByProjectId(projectId)).thenReturn(List.of(testMemo));

        // when
        List<MemoItemResponseDto> result = memoService.findMemosByProjectId(projectId, memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).title()).isEqualTo("테스트 메모");

        verify(projectRepository, times(1)).existsById(projectId);
        verify(accessPolicy, times(1)).ensureProjectMember(projectId, memberId);
        verify(memoRepository, times(1)).findAllByProjectId(projectId);
    }

    @Test
    @DisplayName("findMemosByProjectId: 메모 목록 조회 실패 - 프로젝트 없음")
    void findMemosByProjectId_Fail_ProjectNotFound() {
        // given
        when(projectRepository.existsById(projectId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> memoService.findMemosByProjectId(projectId, memberId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);

        verify(accessPolicy, never()).ensureProjectMember(any(), any());
        verify(memoRepository, never()).findAllByProjectId(any());
    }

    @Test
    @DisplayName("findMemoById: 메모 단건 조회 성공")
    void findMemoById_Success() {
        // given
        doNothing().when(accessPolicy).ensureProjectMember(projectId, memberId);
        when(memoRepository.findById(memoId)).thenReturn(Optional.of(testMemo));
        // testMemo.ensureMemoInProject(projectId)는 성공적으로 통과함

        // when
        MemoResponseDto result = memoService.findMemoById(projectId, memoId, memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(memoId);

        verify(accessPolicy, times(1)).ensureProjectMember(projectId, memberId);
        verify(memoRepository, times(1)).findById(memoId);
    }

    @Test
    @DisplayName("findMemoById: 메모 단건 조회 실패 - 메모가 다른 프로젝트 소속")
    void findMemoById_Fail_MemoNotInProject() {
        // given
        UUID otherProjectId = UUID.randomUUID();
        // 이 메모는 testProject(projectId) 소속임
        when(memoRepository.findById(memoId)).thenReturn(Optional.of(testMemo));
        doNothing().when(accessPolicy).ensureProjectMember(otherProjectId, memberId);

        // when & then
        assertThatThrownBy(() -> memoService.findMemoById(otherProjectId, memoId, memberId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_MEMO_ONLY);

        verify(accessPolicy, times(1)).ensureProjectMember(otherProjectId, memberId);
        verify(memoRepository, times(1)).findById(memoId);
    }

    @Test
    @DisplayName("updateMemo: 메모 수정 성공")
    void updateMemo_Success() {
        // given
        MemoUpdateRequestDto requestDto = new MemoUpdateRequestDto("수정된 제목", "수정된 내용");

        doNothing().when(accessPolicy).ensureProjectMember(projectId, memberId);
        when(memoRepository.findById(memoId)).thenReturn(Optional.of(testMemo));

        // when
        MemoResponseDto result = memoService.updateMemo(projectId, memoId, memberId, requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("수정된 제목");
        assertThat(result.content()).isEqualTo("수정된 내용");

        assertThat(testMemo.getTitle()).isEqualTo("수정된 제목");
        assertThat(testMemo.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("updateMemo: 메모 수정 실패 - 접근 권한 없음")
    void updateMemo_Fail_AccessDenied() {
        // given
        MemoUpdateRequestDto requestDto = new MemoUpdateRequestDto("수정 시도", "수정 시도");

        doThrow(new BusinessException(ErrorCode.PROJECT_MEMBER_ONLY, "권한 없음"))
            .when(accessPolicy).ensureProjectMember(projectId, memberId);

        // when & then
        assertThatThrownBy(() -> memoService.updateMemo(projectId, memoId, memberId, requestDto))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_MEMBER_ONLY);

        verify(memoRepository, never()).findById(any());
        assertThat(testMemo.getTitle()).isEqualTo("테스트 메모");
    }

    @Test
    @DisplayName("updateMemo: 메모 수정 실패 - 메모 없음")
    void updateMemo_Fail_MemoNotFound() {
        // given
        MemoUpdateRequestDto requestDto = new MemoUpdateRequestDto("수정 시도", "수정 시도");

        doNothing().when(accessPolicy).ensureProjectMember(projectId, memberId);
        when(memoRepository.findById(memoId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memoService.updateMemo(projectId, memoId, memberId, requestDto))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMO_NOT_FOUND);

        assertThat(testMemo.getTitle()).isEqualTo("테스트 메모");
    }

    @Test
    @DisplayName("updateMemo: 메모 수정 실패 - 메모가 다른 프로젝트 소속")
    void updateMemo_Fail_MemoNotInProject() {
        // given
        MemoUpdateRequestDto requestDto = new MemoUpdateRequestDto("수정 시도", "수정 시도");
        UUID otherProjectId = UUID.randomUUID();

        doNothing().when(accessPolicy).ensureProjectMember(otherProjectId, memberId);
        when(memoRepository.findById(memoId)).thenReturn(Optional.of(testMemo));

        // when & then
        assertThatThrownBy(
            () -> memoService.updateMemo(otherProjectId, memoId, memberId, requestDto))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_MEMO_ONLY);

        assertThat(testMemo.getTitle()).isEqualTo("테스트 메모");
    }


    @Test
    @DisplayName("deleteMemo: 메모 삭제 성공")
    void deleteMemo_Success() {
        // given
        doNothing().when(accessPolicy).ensureProjectMember(projectId, memberId);
        when(memoRepository.findById(memoId)).thenReturn(Optional.of(testMemo));
        doNothing().when(memoRepository).delete(testMemo);

        // when
        memoService.deleteMemo(projectId, memoId, memberId);

        // then
        verify(accessPolicy, times(1)).ensureProjectMember(projectId, memberId);
        verify(memoRepository, times(1)).findById(memoId);
        verify(memoRepository, times(1)).delete(testMemo);
    }

    @Test
    @DisplayName("deleteMemo: 메모 삭제 실패 - 접근 권한 없음")
    void deleteMemo_Fail_AccessDenied() {
        // given
        doThrow(new BusinessException(ErrorCode.PROJECT_MEMBER_ONLY, "권한 없음"))
            .when(accessPolicy).ensureProjectMember(projectId, memberId);

        // when & then
        assertThatThrownBy(() -> memoService.deleteMemo(projectId, memoId, memberId))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_MEMBER_ONLY);

        verify(memoRepository, never()).findById(any());
        verify(memoRepository, never()).delete(any());
    }
}
