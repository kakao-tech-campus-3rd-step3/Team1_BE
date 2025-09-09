package knu.team1.be.boost.file.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import knu.team1.be.boost.file.dto.FileCompleteRequest;
import knu.team1.be.boost.file.dto.FileCompleteResponse;
import knu.team1.be.boost.file.dto.FileRequest;
import knu.team1.be.boost.file.dto.FileResponse;
import knu.team1.be.boost.file.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FileController implements FileApi {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @Override
    public ResponseEntity<FileResponse> uploadFile(@Valid @RequestBody FileRequest request) {
        FileResponse fileResponse = fileService.uploadFile(request);
        URI location = URI.create("/api/files/" + fileResponse.fileId());
        return ResponseEntity.created(location).body(fileResponse);
    }

    @Override
    public ResponseEntity<FileResponse> downloadFile(@PathVariable UUID fileId) {
        FileResponse fileResponse = fileService.downloadFile(fileId);
        return ResponseEntity.ok(fileResponse);
    }

    @Override
    public ResponseEntity<FileCompleteResponse> completeUpload(
        @PathVariable UUID fileId,
        @Valid @RequestBody FileCompleteRequest request
    ) {
        FileCompleteResponse response = fileService.completeUpload(fileId, request);
        return ResponseEntity.ok(response);
    }
}
