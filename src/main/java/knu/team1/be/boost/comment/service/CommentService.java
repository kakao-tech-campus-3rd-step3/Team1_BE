package knu.team1.be.boost.comment.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import knu.team1.be.boost.comment.dto.CommentCreateRequestDto;
import knu.team1.be.boost.comment.dto.CommentResponseDto;
import knu.team1.be.boost.comment.dto.CommentUpdateRequestDto;
import knu.team1.be.boost.comment.dto.FileInfoRequestDto;
import knu.team1.be.boost.comment.entity.Comment;
import knu.team1.be.boost.comment.entity.vo.FileInfo;
import knu.team1.be.boost.comment.event.CommentEventPublisher;
import knu.team1.be.boost.comment.repository.CommentRepository;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.common.policy.AccessPolicy;
import knu.team1.be.boost.file.repository.FileRepository;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.member.repository.MemberRepository;
import knu.team1.be.boost.task.entity.Task;
import knu.team1.be.boost.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final TaskRepository taskRepository;
    private final FileRepository fileRepository;

    private final AccessPolicy accessPolicy;

    private final CommentEventPublisher commentEventPublisher;

    public List<CommentResponseDto> findCommentsByTaskId(
        UUID projectId,
        UUID memberId,
        UUID taskId
    ) {
        accessPolicy.ensureProjectMember(projectId, memberId);

        List<Comment> comments = commentRepository.findAllByTaskIdOrderByCreatedAtAsc(taskId);
        return comments.stream()
            .map(CommentResponseDto::from)
            .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponseDto createComment(
        UUID projectId,
        UUID taskId,
        UUID memberId,
        CommentCreateRequestDto requestDto
    ) {
        accessPolicy.ensureProjectMember(projectId, memberId);

        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.TASK_NOT_FOUND,
                "taskId: " + taskId
            ));
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.MEMBER_NOT_FOUND,
                "memberId: " + memberId
            ));
        FileInfo fileInfo = createFileInfoFromDto(requestDto.fileInfo());

        Comment comment = Comment.builder()
            .task(task)
            .member(member)
            .content(requestDto.content())
            .persona(requestDto.persona())
            .isAnonymous(requestDto.isAnonymous())
            .fileInfo(fileInfo)
            .build();

        Comment savedComment = commentRepository.save(comment);

        commentEventPublisher.publishCommentCreatedEvent(
            projectId,
            taskId,
            memberId,
            requestDto.content(),
            requestDto.isAnonymous(),
            requestDto.persona() != null ? requestDto.persona().name() : null
        );

        return CommentResponseDto.from(savedComment);
    }

    @Transactional
    public CommentResponseDto updateComment(
        UUID commentId,
        UUID memberId,
        CommentUpdateRequestDto requestDto
    ) {
        Comment comment = findCommentOrThrow(commentId);

        accessPolicy.ensureCommentAuthor(comment.getMember().getId(), memberId);

        comment.updateContent(requestDto.content());

        Optional.ofNullable(requestDto.isAnonymous())
            .ifPresent(comment::updateIsAnonymous);

        Optional.ofNullable(requestDto.fileInfo())
            .map(this::createFileInfoFromDto)
            .ifPresent(comment::updateFileInfo);

        return CommentResponseDto.from(comment);
    }

    @Transactional
    public void deleteComment(UUID commentId, UUID memberId) {
        Comment comment = findCommentOrThrow(commentId);

        accessPolicy.ensureCommentAuthor(comment.getMember().getId(), memberId);

        commentRepository.delete(comment);
    }

    private Comment findCommentOrThrow(UUID commentId) {
        return commentRepository.findById(commentId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.COMMENT_NOT_FOUND,
                "commentId: " + commentId
            ));
    }

    private FileInfo createFileInfoFromDto(FileInfoRequestDto dto) {
        if (dto == null) {
            return null;
        }

        return Optional.ofNullable(dto.fileId())
            .map(fileId -> fileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(
                    ErrorCode.FILE_NOT_FOUND,
                    "fileId: " + fileId
                )))
            .map(file -> new FileInfo(
                file,
                dto.filePage(),
                dto.fileX(),
                dto.fileY()
            ))
            .orElse(null);
    }
}
