package knu.team1.be.boost.tag.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import knu.team1.be.boost.tag.entity.Tag;

@Schema(description = "태그 응답 DTO")
public record TagResponseDto(

    @Schema(description = "태그 ID", example = "770e8400-e29b-41d4-a716-446655440000")
    UUID tagId,

    @Schema(description = "태그 이름", example = "피드백")
    String name
) {
    public static TagResponseDto from(Tag tag) {
        return new TagResponseDto(tag.getId(), tag.getName());
    }
}
