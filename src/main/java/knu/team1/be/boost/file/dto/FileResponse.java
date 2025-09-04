package knu.team1.be.boost.file.dto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import knu.team1.be.boost.file.entity.File;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

public record FileResponse(
    UUID fileId,
    String key,
    String url,
    String method,
    Map<String, String> headers,
    Integer expiresInSeconds
) {

    public static FileResponse forUpload(File file, PresignedPutObjectRequest presigned,
        int expiresInSeconds) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", file.getContentType());
        headers.put("x-amz-server-side-encryption", "AES256");

        return new FileResponse(
            file.getId(),
            file.getStorageKey(),
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
            file.getStorageKey(),
            presigned.url().toString(),
            "GET",
            Collections.emptyMap(),
            expiresInSeconds
        );
    }
}
