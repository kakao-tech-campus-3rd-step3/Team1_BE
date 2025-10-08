package knu.team1.be.boost.comment.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.comment.dto.CommentCreateRequestDto;
import knu.team1.be.boost.comment.dto.CommentResponseDto;
import knu.team1.be.boost.comment.dto.CommentUpdateRequestDto;
import knu.team1.be.boost.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentController implements CommentApi {

    private final CommentService commentService;

    @Override
    public ResponseEntity<List<CommentResponseDto>> getComments(
        @PathVariable UUID projectId,
        @PathVariable UUID taskId,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        List<CommentResponseDto> comments = commentService.findCommentsByTaskId(
            projectId,
            user.id(),
            taskId
        );
        return ResponseEntity.ok(comments);
    }

    @Override
    public ResponseEntity<CommentResponseDto> createComment(
        @PathVariable UUID projectId,
        @PathVariable UUID taskId,
        @AuthenticationPrincipal UserPrincipalDto user,
        @Valid @RequestBody CommentCreateRequestDto request
    ) {
        CommentResponseDto createdComment = commentService.createComment(
            projectId,
            taskId,
            user.id(),
            request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @Override
    public ResponseEntity<CommentResponseDto> updateComment(
        @PathVariable UUID commentId,
        @AuthenticationPrincipal UserPrincipalDto user,
        @Valid @RequestBody CommentUpdateRequestDto request
    ) {
        CommentResponseDto updatedComment = commentService.updateComment(
            commentId,
            user.id(),
            request
        );
        return ResponseEntity.ok(updatedComment);
    }

    @Override
    public ResponseEntity<Void> deleteComment(
        @PathVariable UUID commentId,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        commentService.deleteComment(commentId, user.id());
        return ResponseEntity.noContent().build();
    }
}
