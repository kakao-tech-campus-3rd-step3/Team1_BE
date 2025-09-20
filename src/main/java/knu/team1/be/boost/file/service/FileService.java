package knu.team1.be.boost.file.service;

import java.time.LocalDateTime;
import java.util.UUID;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.file.dto.FileCompleteRequestDto;
import knu.team1.be.boost.file.dto.FileCompleteResponseDto;
import knu.team1.be.boost.file.dto.FileRequestDto;
import knu.team1.be.boost.file.dto.FileResponseDto;
import knu.team1.be.boost.file.entity.File;
import knu.team1.be.boost.file.entity.FileType;
import knu.team1.be.boost.file.entity.vo.StorageKey;
import knu.team1.be.boost.file.infra.s3.PresignedUrlFactory;
import knu.team1.be.boost.file.repository.FileRepository;
import knu.team1.be.boost.task.entity.Task;
import knu.team1.be.boost.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final TaskRepository taskRepository;
    private final PresignedUrlFactory presignedUrlFactory;

    @Value("${boost.aws.bucket}")
    private String bucket;

    @Value("${boost.aws.upload.expire-seconds}")
    private int expireSeconds;

    @Value("${boost.file.max-upload-size}")
    private DataSize maxUploadSize;

    @Transactional
    public FileResponseDto uploadFile(FileRequestDto request) {
        long max = maxUploadSize.toBytes();
        long size = request.sizeBytes();
        if (request.sizeBytes() > max) {
            throw new BusinessException(
                ErrorCode.FILE_TOO_LARGE,
                "size: " + size + ", max: " + max
            );
        }

        FileType fileType = FileType.fromContentType(request.contentType());
        StorageKey key = StorageKey.generate(LocalDateTime.now(), fileType.getExtension());

        File file = File.pendingUpload(request, fileType, key);
        File saved = fileRepository.save(file);

        PresignedPutObjectRequest presigned = presignedUrlFactory.forUpload(
            bucket,
            key.value(),
            request.contentType(),
            expireSeconds
        );

        log.info("파일 업로드 presigned URL 발급 성공 - fileId={}, filename={}",
            saved.getId(), saved.getMetadata().originalFilename());
        return FileResponseDto.forUpload(saved, presigned, expireSeconds);
    }

    @Transactional(readOnly = true)
    public FileResponseDto downloadFile(UUID fileId) {
        // TODO: 다운로드 요청자가 해당 프로젝트 팀원인지 검증

        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.FILE_NOT_FOUND,
                "fileId: " + fileId
            ));

        if (!file.isComplete()) {
            throw new BusinessException(
                ErrorCode.FILE_NOT_READY,
                "fileId: " + fileId
            );
        }

        PresignedGetObjectRequest presigned = presignedUrlFactory.forDownload(
            bucket,
            file.getStorageKey().value(),
            file.getMetadata().originalFilename(),
            file.getMetadata().contentType(),
            expireSeconds
        );

        log.info("파일 다운로드 presigned URL 발급 성공 - fileId={}, filename={}",
            file.getId(), file.getMetadata().originalFilename());
        return FileResponseDto.forDownload(file, presigned, expireSeconds);
    }

    @Transactional
    public FileCompleteResponseDto completeUpload(UUID fileId, FileCompleteRequestDto request) {
        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.FILE_NOT_FOUND,
                "fileId: " + fileId
            ));

        if (file.isComplete()) {
            throw new BusinessException(
                ErrorCode.FILE_ALREADY_UPLOAD_COMPLETED,
                "fileId: " + fileId
            );
        }

        long max = maxUploadSize.toBytes();
        long size = request.sizeBytes();
        if (request.sizeBytes() > max) {
            throw new BusinessException(
                ErrorCode.FILE_TOO_LARGE,
                "fileId: " + fileId + ", size: " + size + ", max: " + max
            );
        }

        file.getMetadata()
            .validateMatches(request.filename(), request.contentType(), request.sizeBytes());

        UUID taskId = request.taskId();
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.TASK_NOT_FOUND,
                "파일 업로드 완료 실패 " + "taskId: " + taskId + ", fileId: " + fileId
            ));

        file.assignTask(task);
        file.complete();

        log.info("파일 업로드 완료 처리 성공 - fileId={}, taskId={}, filename={}",
            fileId, taskId, request.filename());
        return FileCompleteResponseDto.from(file, taskId);
    }
}
