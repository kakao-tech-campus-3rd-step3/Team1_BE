package knu.team1.be.boost.file.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import knu.team1.be.boost.file.dto.FileRequest;
import knu.team1.be.boost.file.dto.FileResponse;
import knu.team1.be.boost.file.entity.File;
import knu.team1.be.boost.file.entity.FileStatus;
import knu.team1.be.boost.file.entity.FileType;
import knu.team1.be.boost.file.repository.FileRespository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final S3Presigner s3Presigner;
    private final FileRespository fileRepository;

    @Value("${boost.aws.bucket}")
    private String bucket;

    @Value("${boost.aws.region}")
    private String region;

    @Value("${boost.aws.upload.expire-seconds}")
    private int expireSeconds;

    @Override
    @Transactional
    public FileResponse uploadFile(FileRequest fileRequest) {

        FileType fileType = FileType.fromContentType(fileRequest.contentType());
        String extension = fileType.getExtension();

        String key = createKey(extension);

        File file = File.builder()
            .user(null)     // TODO: 인증 붙이면 채우기
            .originalFilename(fileRequest.filename())
            .type(fileType)
            .contentType(fileRequest.contentType())
            .sizeBytes(fileRequest.sizeBytes())
            .storageKey(key)
            .status(FileStatus.PENDING)
            .build();

        File savedFile = fileRepository.save(file);

        PutObjectRequest putReq = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(fileRequest.contentType())
            .serverSideEncryption(ServerSideEncryption.AES256)
            .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(builder -> builder
            .putObjectRequest(putReq)
            .signatureDuration(Duration.ofSeconds(expireSeconds)));

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", fileRequest.contentType());
        headers.put("x-amz-server-side-encryption", "AES256");

        return new FileResponse(
            savedFile.getId(),
            key,
            presigned.url().toString(),
            "PUT",
            headers,
            expireSeconds
        );
    }

    private String createKey(String extension) {
        LocalDateTime now = LocalDateTime.now();

        return String.format(
            "file/%04d/%02d/%02d/%s.%s",
            now.getYear(),
            now.getMonthValue(),
            now.getDayOfMonth(),
            UUID.randomUUID(),
            extension
        );
    }
}
