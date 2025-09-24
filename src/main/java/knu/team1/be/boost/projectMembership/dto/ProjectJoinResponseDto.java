package knu.team1.be.boost.projectMembership.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record ProjectJoinResponseDto(

    @Schema(description = "참여한 프로젝트 고유 ID", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    UUID projectId
    
) {

}
