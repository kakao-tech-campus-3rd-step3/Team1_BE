package knu.team1.be.boost.ai.controller;

import jakarta.validation.Valid;
import knu.team1.be.boost.ai.dto.AiCommentTransformRequestDto;
import knu.team1.be.boost.ai.dto.AiCommentTransformResponseDto;
import knu.team1.be.boost.ai.service.AiCommentTransformService;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AiCommentController implements AiCommentApi {

    private final AiCommentTransformService aiCommentTransformService;

    @Override
    public ResponseEntity<AiCommentTransformResponseDto> transformComment(
        @AuthenticationPrincipal UserPrincipalDto user,
        @Valid @RequestBody AiCommentTransformRequestDto request
    ) {
        AiCommentTransformResponseDto response = aiCommentTransformService.transformComment(request);
        return ResponseEntity.ok(response);
    }
}

