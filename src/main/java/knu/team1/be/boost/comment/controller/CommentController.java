package knu.team1.be.boost.comment.controller;

import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.comment.dto.CommentResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentController implements CommentApi {

    @Override
    public ResponseEntity<List<CommentResponseDto>> getComments(
        UUID taskId,
        UserPrincipalDto user
    ) {
        return null;
    }

    @Override
    public ResponseEntity<CommentResponseDto> getComment(UUID commentId, UserPrincipalDto user) {
        return null;
    }

    @Override
    public ResponseEntity<CommentResponseDto> createComment(UUID taskId, UserPrincipalDto user) {
        return null;
    }

    @Override
    public ResponseEntity<CommentResponseDto> updateComment(UUID commentId, UserPrincipalDto user) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteComment(UUID commentId, UserPrincipalDto user) {
        return null;
    }
}
