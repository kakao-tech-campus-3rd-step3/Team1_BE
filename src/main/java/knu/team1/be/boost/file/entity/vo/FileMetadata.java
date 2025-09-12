package knu.team1.be.boost.file.entity.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record FileMetadata(

    @Column(name = "original_filename", nullable = false)
    String originalFilename,

    @Column(name = "content_type", nullable = false, length = 100)
    String contentType,

    @Column(name = "size_bytes", nullable = false)
    Integer sizeBytes

) {

    public static final int MAX_FILENAME_LENGTH = 255;
    public static final int MAX_CONTENT_TYPE_LENGTH = 100;

    public static FileMetadata of(String originalFilename, String contentType, Integer sizeBytes) {
        validateLength(originalFilename, contentType);
        return new FileMetadata(originalFilename, contentType, sizeBytes);
    }

    private static void validateLength(String originalFilename, String contentType) {
        if (originalFilename.length() > MAX_FILENAME_LENGTH) {
            throw new IllegalArgumentException(
                String.format("파일명이 너무 깁니다. 최대 %d자까지 허용됩니다.", MAX_FILENAME_LENGTH));
        }
        if (contentType.length() > MAX_CONTENT_TYPE_LENGTH) {
            throw new IllegalArgumentException(
                String.format("ContentType이 너무 깁니다. 최대 %d자까지 허용됩니다.", MAX_CONTENT_TYPE_LENGTH));
        }
    }

    public void validateMatches(String filename, String contentType, Integer sizeBytes) {
        if (!this.originalFilename.equals(filename)) {
            throw new IllegalArgumentException("파일명이 일치하지 않습니다.");
        }
        if (!this.contentType.equals(contentType)) {
            throw new IllegalArgumentException("ContentType이 일치하지 않습니다.");
        }
        if (!this.sizeBytes.equals(sizeBytes)) {
            throw new IllegalArgumentException("파일 크기가 일치하지 않습니다.");
        }
    }
}
