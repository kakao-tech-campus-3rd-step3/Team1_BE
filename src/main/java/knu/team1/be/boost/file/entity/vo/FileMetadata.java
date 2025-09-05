package knu.team1.be.boost.file.entity.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record FileMetadata(

    @Column(name = "original_filename", nullable = false, columnDefinition = "text")
    String originalFilename,

    @Column(name = "content_type", nullable = false, length = 100)
    String contentType,

    @Column(name = "size_bytes", nullable = false)
    Integer sizeBytes

) {

    public static FileMetadata of(String originalFilename, String contentType, Integer sizeBytes) {
        return new FileMetadata(originalFilename, contentType, sizeBytes);
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
