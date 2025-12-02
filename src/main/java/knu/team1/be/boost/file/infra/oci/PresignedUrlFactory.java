package knu.team1.be.boost.file.infra.oci;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class PresignedUrlFactory {

    private final S3Presigner s3Presigner;

    private static final int MAX_FILENAME_LENGTH = 255;
    private static final String DEFAULT_FILENAME = "download";

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
                .build();

            return s3Presigner.presignPutObject(b -> b
                .putObjectRequest(putReq)
                .signatureDuration(Duration.ofSeconds(expireSeconds)));
        } catch (SdkException e) {
            throw new BusinessException(
                ErrorCode.STORAGE_SERVICE_ERROR,
                "OCI upload presigned URL creation fail. key: " + key
            );
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
            throw new BusinessException(
                ErrorCode.STORAGE_SERVICE_ERROR,
                "OCI download presigned URL creation fail. key: " + key
            );
        }
    }

    // 파일명 정규화
    private static String sanitizeFilename(String name) {
        if (name == null) {
            return DEFAULT_FILENAME;
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
        if (s.length() > MAX_FILENAME_LENGTH) {
            s = s.substring(0, MAX_FILENAME_LENGTH);
        }

        // 빈 문자열 처리
        if (s.isBlank()) {
            s = DEFAULT_FILENAME;
        }
        return s;
    }
}

