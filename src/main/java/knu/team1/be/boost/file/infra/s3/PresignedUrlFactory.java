package knu.team1.be.boost.file.infra.s3;

import java.time.Duration;
import knu.team1.be.boost.file.exception.StorageServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class PresignedUrlFactory {

    private final S3Presigner s3Presigner;

    public PresignedPutObjectRequest forUpload(
        String bucket,
        String key,
        String contentType,
        int expireSeconds
    ) {
        try {
            PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .serverSideEncryption(ServerSideEncryption.AES256)
                .build();

            return s3Presigner.presignPutObject(b -> b
                .putObjectRequest(putReq)
                .signatureDuration(Duration.ofSeconds(expireSeconds)));
        } catch (SdkException e) {
            log.error("S3 업로드 presigned URL 생성 실패 - key={}", key, e);
            throw new StorageServiceException("S3 업로드 URL 생성에 실패했습니다.");
        }
    }

    public PresignedGetObjectRequest forDownload(
        String bucket,
        String key,
        String originalFilename,
        String contentType,
        int expireSeconds
    ) {
        try {
            GetObjectRequest getReq = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .responseContentDisposition("attachment; filename=\"" + originalFilename + "\"")
                .responseContentType(contentType)
                .build();

            return s3Presigner.presignGetObject(b -> b
                .getObjectRequest(getReq)
                .signatureDuration(Duration.ofSeconds(expireSeconds)));
        } catch (SdkException e) {
            log.error("S3 다운로드 presigned URL 생성 실패 - key={}", key, e);
            throw new StorageServiceException("S3 다운로드 URL 생성에 실패했습니다.");
        }
    }
}
