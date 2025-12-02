package knu.team1.be.boost.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
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
import knu.team1.be.boost.comment.dto.CommentCreateRequestDto;
import knu.team1.be.boost.comment.dto.CommentResponseDto;
import knu.team1.be.boost.comment.dto.CommentUpdateRequestDto;
import knu.team1.be.boost.comment.dto.FileInfoRequestDto;
import knu.team1.be.boost.comment.entity.Comment;
import knu.team1.be.boost.comment.entity.Persona;
import knu.team1.be.boost.comment.entity.vo.FileInfo;
import knu.team1.be.boost.comment.event.CommentEventPublisher;
import knu.team1.be.boost.comment.repository.CommentRepository;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.common.policy.AccessPolicy;
import knu.team1.be.boost.file.entity.File;
import knu.team1.be.boost.file.repository.FileRepository;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.member.repository.MemberRepository;
import knu.team1.be.boost.task.entity.Task;
import knu.team1.be.boost.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private FileRepository fileRepository;
    @Mock
    private AccessPolicy accessPolicy;
    @Mock
    private CommentEventPublisher commentEventPublisher;

    // 테스트용 상수 데이터
    private final UUID projectId = UUID.randomUUID();
    private final UUID taskId = UUID.randomUUID();
    private final UUID memberId = UUID.randomUUID();
    private final UUID commentId = UUID.randomUUID();
    private final UUID fileId = UUID.randomUUID();

    private Member testMember;
    private Task testTask;
    private File testFile;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
            .id(memberId)
            .name("테스트유저")
            .avatar("1111")
            .build();

        testTask = Task.builder()
            .id(taskId)
            .build();

        testFile = File.builder()
            .id(fileId)
            .build();

        testComment = Comment.builder()
            .id(commentId)
            .member(testMember)
            .task(testTask)
            .content("원본 댓글")
            .persona(Persona.BOO)
            .isAnonymous(false)
            .fileInfo(null)
            .createdAt(LocalDateTime.now().minusDays(1))
            .updatedAt(LocalDateTime.now().minusDays(1))
            .build();
    }

    @Test
    @DisplayName("findCommentsByTaskId: 댓글 목록 조회 성공")
    void findCommentsByTaskId_Success() {
        // given
        List<Comment> comments = List.of(testComment);

        doNothing().when(accessPolicy).ensureProjectMember(projectId, memberId);
        when(commentRepository.findAllByTaskIdOrderByCreatedAtAsc(taskId)).thenReturn(comments);

        // when
        List<CommentResponseDto> result = commentService.findCommentsByTaskId(
            projectId,
            memberId,
            taskId
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).content()).isEqualTo("원본 댓글");
        assertThat(result.get(0).authorInfo().name()).isEqualTo("테스트유저");
        assertThat(result.get(0).isAnonymous()).isFalse();

        verify(accessPolicy, times(1)).ensureProjectMember(projectId, memberId);
        verify(commentRepository, times(1)).findAllByTaskIdOrderByCreatedAtAsc(taskId);
    }

    @Test
    @DisplayName("createComment: 댓글 생성 성공 (파일 없음)")
    void createComment_Success_NoFile() {
        // given
        CommentCreateRequestDto requestDto = new CommentCreateRequestDto(
            "새 댓글",
            Persona.BOO,
            true,
            null
        );

        // save 호출 시 반환될 엔티티 (ID가 부여된 상태)
        Comment savedComment = Comment.builder()
            .id(UUID.randomUUID())
            .member(testMember)
            .task(testTask)
            .content(requestDto.content())
            .persona(requestDto.persona())
            .isAnonymous(requestDto.isAnonymous())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        doNothing().when(accessPolicy).ensureProjectMember(projectId, memberId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        // when
        CommentResponseDto result = commentService.createComment(projectId, taskId, memberId,
            requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("새 댓글");
        assertThat(result.isAnonymous()).isTrue();
        assertThat(result.authorInfo().name()).isEqualTo("익명"); // 익명 true
        assertThat(result.fileInfo()).isNull();

        verify(accessPolicy, times(1)).ensureProjectMember(projectId, memberId);
        verify(taskRepository, times(1)).findById(taskId);
        verify(memberRepository, times(1)).findById(memberId);
        verify(commentRepository, times(1)).save(any(Comment.class));
        verify(fileRepository, never()).findById(any());
    }

    @Test
    @DisplayName("createComment: 댓글 생성 성공 (파일 포함)")
    void createComment_Success_WithFile() {
        // given
        FileInfoRequestDto fileDto = new FileInfoRequestDto(
            fileId,
            1,
            100.5f,
            200.0f
        );
        CommentCreateRequestDto requestDto = new CommentCreateRequestDto(
            "파일 댓글",
            Persona.BOO,
            false,
            fileDto
        );

        Comment savedComment = Comment.builder()
            .id(UUID.randomUUID())
            .member(testMember)
            .task(testTask)
            .content(requestDto.content())
            .persona(requestDto.persona())
            .isAnonymous(requestDto.isAnonymous())
            .fileInfo(new FileInfo(
                testFile,
                fileDto.filePage(),
                fileDto.fileX(),
                fileDto.fileY())
            )
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        doNothing().when(accessPolicy).ensureProjectMember(projectId, memberId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(testFile));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        // when
        CommentResponseDto result = commentService.createComment(projectId, taskId, memberId,
            requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("파일 댓글");
        assertThat(result.fileInfo()).isNotNull();
        assertThat(result.fileInfo().fileId()).isEqualTo(fileId);
        assertThat(result.fileInfo().filePage()).isEqualTo(1);

        verify(fileRepository, times(1)).findById(fileId);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("createComment: 댓글 생성 시 CommentEventPublisher 호출됨")
    void createComment_PublishesEvent() {
        // given
        CommentCreateRequestDto requestDto = new CommentCreateRequestDto(
            "이벤트 테스트 댓글",
            Persona.BOO,
            false,
            null
        );

        Comment savedComment = Comment.builder()
            .id(UUID.randomUUID())
            .member(testMember)
            .task(testTask)
            .content(requestDto.content())
            .persona(requestDto.persona())
            .isAnonymous(requestDto.isAnonymous())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        doNothing().when(accessPolicy).ensureProjectMember(projectId, memberId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);
        doNothing().when(commentEventPublisher)
            .publishCommentCreatedEvent(any(), any(), any(), any(), anyBoolean(), any());

        // when
        CommentResponseDto result = commentService.createComment(projectId, taskId, memberId,
            requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("이벤트 테스트 댓글");

        verify(commentEventPublisher, times(1)).publishCommentCreatedEvent(
            eq(projectId),
            eq(taskId),
            eq(memberId),
            eq(requestDto.content()),
            eq(requestDto.isAnonymous()),
            eq(requestDto.persona() != null ? requestDto.persona().name() : null)
        );
    }

    @Test
    @DisplayName("createComment: 댓글 생성 실패 - FileInfo의 File ID 없음")
    void createComment_Fail_FileNotFoundInFileInfo() {
        // given
        FileInfoRequestDto fileDto = new FileInfoRequestDto(
            fileId,
            1,
            100.5f,
            200.0f
        );
        CommentCreateRequestDto requestDto = new CommentCreateRequestDto(
            "파일 댓글",
            Persona.BOO,
            false,
            fileDto
        );

        doNothing().when(accessPolicy).ensureProjectMember(projectId, memberId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(fileRepository.findById(fileId)).thenReturn(Optional.empty()); // 파일을 찾을 수 없음

        // when & then
        assertThatThrownBy(
            () -> commentService.createComment(projectId, taskId, memberId, requestDto))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_NOT_FOUND);

        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateComment: 댓글 수정 성공")
    void updateComment_Success() {
        // given
        CommentUpdateRequestDto requestDto = new CommentUpdateRequestDto("수정된 댓글", true, null);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        doNothing().when(accessPolicy).ensureCommentAuthor(memberId, memberId);

        // when
        CommentResponseDto result = commentService.updateComment(commentId, memberId, requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("수정된 댓글");
        assertThat(result.isAnonymous()).isTrue();
        assertThat(result.authorInfo().name()).isEqualTo("익명");

        assertThat(testComment.getContent()).isEqualTo("수정된 댓글");
        assertThat(testComment.getIsAnonymous()).isTrue();

        verify(commentRepository, times(1)).findById(commentId);
        verify(accessPolicy, times(1)).ensureCommentAuthor(memberId, memberId);
    }

    @Test
    @DisplayName("updateComment: 댓글 수정 실패 - 댓글 작성자 아님")
    void updateComment_Fail_NotAuthor() {
        // given
        UUID otherMemberId = UUID.randomUUID();
        CommentUpdateRequestDto requestDto = new CommentUpdateRequestDto("수정 시도", true, null);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        // ensureCommentAuthor 호출 시 예외 발생
        doThrow(new BusinessException(ErrorCode.COMMENT_AUTHOR_ONLY, "권한 없음"))
            .when(accessPolicy).ensureCommentAuthor(memberId, otherMemberId);

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(commentId, otherMemberId, requestDto))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_AUTHOR_ONLY);

        verify(commentRepository, times(1)).findById(commentId);
        verify(accessPolicy, times(1)).ensureCommentAuthor(memberId, otherMemberId);
        assertThat(testComment.getContent()).isEqualTo("원본 댓글");
    }

    @Test
    @DisplayName("deleteComment: 댓글 삭제 성공")
    void deleteComment_Success() {
        // given
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        doNothing().when(accessPolicy).ensureCommentAuthor(memberId, memberId);
        doNothing().when(commentRepository).delete(testComment);

        // when
        commentService.deleteComment(commentId, memberId);

        // then
        verify(commentRepository, times(1)).findById(commentId);
        verify(accessPolicy, times(1)).ensureCommentAuthor(memberId, memberId);
        verify(commentRepository, times(1)).delete(testComment);
    }
}
