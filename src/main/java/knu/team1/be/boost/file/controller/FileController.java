package knu.team1.be.boost.file.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.file.dto.FileCompleteRequestDto;
import knu.team1.be.boost.file.dto.FileCompleteResponseDto;
import knu.team1.be.boost.file.dto.FileRequestDto;
import knu.team1.be.boost.file.dto.FileResponseDto;
import knu.team1.be.boost.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FileController implements FileApi {

    private final FileService fileService;

    @Override
    public ResponseEntity<FileResponseDto> uploadFile(
        @Valid @RequestBody FileRequestDto request,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        FileResponseDto response = fileService.uploadFile(request, user);
        URI location = URI.create("/api/files/" + response.fileId());
        return ResponseEntity.created(location).body(response);
    }

    @Override
    public ResponseEntity<FileResponseDto> downloadFile(
        @PathVariable UUID fileId,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        FileResponseDto response = fileService.downloadFile(fileId, user);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<FileCompleteResponseDto> completeUpload(
        @PathVariable UUID fileId,
        @Valid @RequestBody FileCompleteRequestDto request,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        FileCompleteResponseDto response = fileService.completeUpload(fileId, request, user);
        return ResponseEntity.ok(response);
    }
}
