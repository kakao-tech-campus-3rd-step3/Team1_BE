package knu.team1.be.boost.file.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import knu.team1.be.boost.file.dto.FileCompleteRequest;
import knu.team1.be.boost.file.dto.FileCompleteResponse;
import knu.team1.be.boost.file.dto.FileRequest;
import knu.team1.be.boost.file.dto.FileResponse;
import knu.team1.be.boost.file.entity.File;
import knu.team1.be.boost.file.entity.FileStatus;
import knu.team1.be.boost.file.entity.FileType;
import knu.team1.be.boost.file.entity.vo.StorageKey;
import knu.team1.be.boost.file.repository.FileRespository;
import knu.team1.be.boost.task.entity.Task;
import knu.team1.be.boost.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final S3Presigner s3Presigner;
    private final FileRespository fileRepository;
    private final TaskRepository taskRepository;

    @Value("${boost.aws.bucket}")
    private String bucket;

    @Value("${boost.aws.region}")
    private String region;

    @Value("${boost.aws.upload.expire-seconds}")
    private int expireSeconds;

    @Override
    @Transactional
    public FileResponse uploadFile(FileRequest request) {
        FileType fileType = FileType.fromContentType(request.contentType());
        StorageKey key = StorageKey.generate(LocalDateTime.now(), fileType.getExtension());

        File file = File.pendingUpload(request, fileType, key);
        File saved = fileRepository.save(file);

        PutObjectRequest putReq = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key.value())
            .contentType(request.contentType())
            .serverSideEncryption(ServerSideEncryption.AES256)
            .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(b -> b
            .putObjectRequest(putReq)
            .signatureDuration(Duration.ofSeconds(expireSeconds)));

        return FileResponse.forUpload(saved, presigned, expireSeconds);
    }

    @Override
    @Transactional(readOnly = true)
    public FileResponse downloadFile(UUID fileId) {
        // TODO: 다운로드 요청자가 해당 프로젝트 팀원인지 검증

        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다: " + fileId));

        if (file.getStatus() != FileStatus.COMPLETED) {
            throw new IllegalArgumentException("아직 다운로드할 수 없는 상태의 파일입니다.");
        }

        GetObjectRequest getReq = GetObjectRequest.builder()
            .bucket(bucket)
            .key(file.getStorageKey().value())
            .responseContentDisposition(
                "attachment; filename=\"" + file.getMetadata().originalFilename() + "\"")
            .responseContentType(file.getMetadata().contentType())
            .build();

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(b -> b
            .getObjectRequest(getReq)
            .signatureDuration(Duration.ofSeconds(expireSeconds)));

        return FileResponse.forDownload(file, presigned, expireSeconds);
    }

    @Override
    @Transactional
    public FileCompleteResponse completeUpload(UUID fileId,
        FileCompleteRequest request) {
        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다: " + fileId));

        if (file.getStatus() == FileStatus.COMPLETED) {
            throw new IllegalStateException("이미 업로드 완료된 파일입니다.");
        }

        file.getMetadata()
            .validateMatches(request.filename(), request.contentType(), request.sizeBytes());

        UUID taskId = UUID.fromString(request.taskId());
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("할 일을 찾을 수 없습니다: " + taskId));

        file.assignTask(task);
        file.complete();

        return FileCompleteResponse.from(file, taskId);
    }
}
