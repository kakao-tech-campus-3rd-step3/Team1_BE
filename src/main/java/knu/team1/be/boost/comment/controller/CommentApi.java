package knu.team1.be.boost.comment.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.comment.dto.CommentResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Comments", description = "Comment 관련 API")
@RequestMapping("/api")
@SecurityRequirement(name = "bearerAuth")
public interface CommentApi {

    @GetMapping("/task/{taskId}/comments")
    ResponseEntity<List<CommentResponseDto>> getComments(
        @PathVariable UUID taskId,
        @AuthenticationPrincipal UserPrincipalDto user
    );

    @GetMapping("/comments/{commentId}")
    ResponseEntity<CommentResponseDto> getComment(
        @PathVariable UUID commentId,
        @AuthenticationPrincipal UserPrincipalDto user
    );

    @PostMapping("/task/{taskId}/comments")
    ResponseEntity<CommentResponseDto> createComment(
        @PathVariable UUID taskId,
        @AuthenticationPrincipal UserPrincipalDto user
    );

    @PutMapping("/comments/{commentId}")
    ResponseEntity<CommentResponseDto> updateComment(
        @PathVariable UUID commentId,
        @AuthenticationPrincipal UserPrincipalDto user
    );

    @DeleteMapping("/comments/{commentId}")
    ResponseEntity<Void> deleteComment(
        @PathVariable UUID commentId,
        @AuthenticationPrincipal UserPrincipalDto user
    );

}
