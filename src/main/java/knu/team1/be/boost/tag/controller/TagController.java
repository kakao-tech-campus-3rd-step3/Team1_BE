package knu.team1.be.boost.tag.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.tag.dto.TagCreateRequestDto;
import knu.team1.be.boost.tag.dto.TagResponseDto;
import knu.team1.be.boost.tag.dto.TagUpdateRequestDto;
import knu.team1.be.boost.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TagController implements TagApi {

    private final TagService tagService;

    @Override
    public ResponseEntity<TagResponseDto> createTag(
        @PathVariable UUID projectId,
        @Valid @RequestBody TagCreateRequestDto request,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        TagResponseDto response = tagService.createTag(projectId, request, user);
        URI location = URI.create("/api/projects/" + projectId + "/tags/" + response.tagId());
        return ResponseEntity.created(location).body(response);
    }

    @Override
    public ResponseEntity<TagResponseDto> updateTag(
        @PathVariable UUID projectId,
        @PathVariable UUID tagId,
        @Valid @RequestBody TagUpdateRequestDto request,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        TagResponseDto response = tagService.updateTag(projectId, tagId, request, user);
        return ResponseEntity.ok(response);
    }
}
