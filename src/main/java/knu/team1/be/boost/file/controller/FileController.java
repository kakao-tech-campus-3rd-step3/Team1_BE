package knu.team1.be.boost.file.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import knu.team1.be.boost.file.dto.FileCompleteRequestDto;
import knu.team1.be.boost.file.dto.FileCompleteResponseDto;
import knu.team1.be.boost.file.dto.FileRequestDto;
import knu.team1.be.boost.file.dto.FileResponseDto;
import knu.team1.be.boost.file.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Files", description = "파일 관련 API")
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @Operation(
        summary = "파일 메타 생성 + 업로드 Presigned URL 발급",
        description = "files 테이블에 메타 정보를 생성하고, 업로드 가능한 S3 Presigned URL을 반환합니다."
    )
    @PostMapping("/upload-url")
    public ResponseEntity<FileResponseDto> uploadFile(
        @Valid @RequestBody FileRequestDto request
    ) {
        FileResponseDto fileResponseDto = fileService.uploadFile(request);
        URI location = URI.create("/api/files/" + fileResponseDto.fileId());

        return ResponseEntity
            .created(location)
            .body(fileResponseDto);
    }

    @Operation(
        summary = "다운로드 Presigned URL 발급",
        description = "파일 ID를 기반으로 다운로드 가능한 S3 Presigned URL을 반환합니다."
    )
    @GetMapping("/{fileId}/download-url")
    public ResponseEntity<FileResponseDto> downloadFile(
        @PathVariable UUID fileId
    ) {
        FileResponseDto fileResponseDto = fileService.downloadFile(fileId);

        return ResponseEntity.ok(fileResponseDto);
    }

    @Operation(
        summary = "업로드 완료 콜백",
        description = "파일 업로드가 완료되었음을 서버에 알리고 상태를 COMPLETED로 갱신합니다."
    )
    @PatchMapping("/{fileId}/complete")
    public ResponseEntity<FileCompleteResponseDto> completeUpload(
        @PathVariable UUID fileId,
        @Valid @RequestBody FileCompleteRequestDto request
    ) {
        FileCompleteResponseDto response = fileService.completeUpload(fileId, request);

        return ResponseEntity.ok(response);
    }
}
