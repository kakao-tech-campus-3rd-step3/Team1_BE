package knu.team1.be.boost.file.entity.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import java.util.UUID;

@Embeddable
public record StorageKey(
    @Column(name = "storage_key", nullable = false)
    String value
) {

    public static StorageKey generate(LocalDateTime now, String extension) {
        String v = String.format(
            "file/%04d/%02d/%02d/%s.%s",
            now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
            UUID.randomUUID(), extension
        );
        return new StorageKey(v);
    }
}
