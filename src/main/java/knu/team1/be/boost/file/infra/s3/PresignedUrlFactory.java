package knu.team1.be.boost.file.infra.s3;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import knu.team1.be.boost.file.exception.StorageServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
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
            String safeName = sanitizeFilename(originalFilename);
            String contentDisposition = ContentDisposition
                .attachment()
                // 파일명 RFC 6266/5987 형식으로 전달
                .filename(safeName, StandardCharsets.UTF_8)
                .build()
                .toString();

            GetObjectRequest getReq = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .responseContentDisposition(contentDisposition)
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

    // 파일명 정규화
    private static String sanitizeFilename(String name) {
        if (name == null) {
            return "download";
        }

        String s = name
            // 개행/제어문자 제거
            .replaceAll("[\\r\\n\\t\\f\\u0000-\\u001F\\u007F]", "")
            // 메타문자 처리
            .replace("\"", "'")
            .replace(";", "")
            .replace("\\", "")
            // 경로 구분자/트래버설 무력화
            .replace("/", "_")
            .replace(":", "_")
            // 좌우 공백 제거
            .trim();

        // 반복되는 .. -> . 으로 치환
        while (s.contains("..")) {
            s = s.replace("..", ".");
        }

        // 내부 과도한 공백 제거
        s = s.replaceAll(" {2,}", " ");

        // 길이 제한
        int max = 50;
        if (s.length() > max) {
            s = s.substring(0, max);
        }

        // 빈 문자열 처리
        if (s.isBlank()) {
            s = "download";
        }
        return s;
    }
}
