package knu.team1.be.boost.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import knu.team1.be.boost.file.entity.File;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Schema(description = "파일 업로드/다운로드 Presigned URL 응답 DTO")
public record FileResponse(

    @Schema(description = "파일 ID (UUID)", example = "2f8f2a2e-4a63-4f3a-8d1b-2a4de6d6f8aa")
    UUID fileId,

    @Schema(description = "스토리지 오브젝트 키(경로)", example = "uploads/2025/09/05/2f8f2a2e-4a63-4f3a-8d1b-2a4de6d6f8aa.pdf")
    String key,

    @Schema(description = "Presigned URL", example = "https://boost-s3-bucket-storage.s3.ap-northeast-2.amazonaws.com/...")
    String url,

    @Schema(description = "요청 메서드 (PUT/GET)", example = "PUT")
    String method,

    @Schema(
        description = "요청 시 포함해야 할 헤더들(키-값)",
        example = "{\"Content-Type\":\"application/pdf\",\"x-amz-server-side-encryption\":\"AES256\"}"
    )
    Map<String, String> headers,

    @Schema(description = "URL 만료 시간(초)", example = "300")
    Integer expiresInSeconds
) {

    public static FileResponse forUpload(File file, PresignedPutObjectRequest presigned,
        int expiresInSeconds) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", file.getMetadata().contentType());
        headers.put("x-amz-server-side-encryption", "AES256");

        return new FileResponse(
            file.getId(),
            file.getStorageKey().value(),
            presigned.url().toString(),
            "PUT",
            headers,
            expiresInSeconds
        );
    }

    public static FileResponse forDownload(File file, PresignedGetObjectRequest presigned,
        int expiresInSeconds) {
        return new FileResponse(
            file.getId(),
            file.getStorageKey().value(),
            presigned.url().toString(),
            "GET",
            Collections.emptyMap(),
            expiresInSeconds
        );
    }
}
