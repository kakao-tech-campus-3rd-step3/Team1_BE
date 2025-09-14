package knu.team1.be.boost.file.service;

import java.time.LocalDateTime;
import java.util.UUID;
import knu.team1.be.boost.file.dto.FileCompleteRequestDto;
import knu.team1.be.boost.file.dto.FileCompleteResponseDto;
import knu.team1.be.boost.file.dto.FileRequestDto;
import knu.team1.be.boost.file.dto.FileResponseDto;
import knu.team1.be.boost.file.entity.File;
import knu.team1.be.boost.file.entity.FileType;
import knu.team1.be.boost.file.entity.vo.StorageKey;
import knu.team1.be.boost.file.exception.FileAlreadyUploadCompletedException;
import knu.team1.be.boost.file.exception.FileNotFoundException;
import knu.team1.be.boost.file.exception.FileNotReadyException;
import knu.team1.be.boost.file.exception.FileTooLargeException;
import knu.team1.be.boost.file.infra.s3.PresignedUrlFactory;
import knu.team1.be.boost.file.repository.FileRepository;
import knu.team1.be.boost.task.entity.Task;
import knu.team1.be.boost.task.exception.TaskNotFoundException;
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
            log.warn("파일 업로드 실패 - 최대 크기 초과 size={}, max={}", size, max);
            throw new FileTooLargeException(size, max);
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
            .orElseThrow(() -> {
                log.warn("파일 다운로드 실패 - 존재하지 않는 fileId={}", fileId);
                return new FileNotFoundException(fileId);
            });

        if (!file.isComplete()) {
            log.warn("파일 다운로드 실패 - 업로드 미완료 fileId={}", fileId);
            throw new FileNotReadyException(fileId);
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
            .orElseThrow(() -> {
                log.warn("파일 업로드 완료 실패 - 존재하지 않는 fileId={}", fileId);
                return new FileNotFoundException(fileId);
            });

        if (file.isComplete()) {
            log.warn("파일 업로드 완료 실패 - 이미 업로드 완료 처리된 파일 fileId={}", fileId);
            throw new FileAlreadyUploadCompletedException(fileId);
        }

        long max = maxUploadSize.toBytes();
        long size = request.sizeBytes();
        if (request.sizeBytes() > max) {
            log.warn("파일 업로드 완료 실패 - 최대 크기 초과 fileId={}, size={}, max={}",
                fileId, size, max);
            throw new FileTooLargeException(size, max);
        }

        file.getMetadata()
            .validateMatches(request.filename(), request.contentType(), request.sizeBytes());

        UUID taskId = request.taskId();
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> {
                log.warn("파일 업로드 완료 실패 - 존재하지 않는 taskId={}, fileId={}", taskId, fileId);
                return new TaskNotFoundException(taskId);
            });

        file.assignTask(task);
        file.complete();

        log.info("파일 업로드 완료 처리 성공 - fileId={}, taskId={}, filename={}",
            fileId, taskId, request.filename());
        return FileCompleteResponseDto.from(file, taskId);
    }
}
