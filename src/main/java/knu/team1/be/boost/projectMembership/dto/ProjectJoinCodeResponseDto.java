package knu.team1.be.boost.projectMembership.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import knu.team1.be.boost.projectMembership.entity.ProjectJoinCode;

public record ProjectJoinCodeResponseDto(

    @Schema(description = "프로젝트 참여 코드", example = "A1B2C3")
    String joinCode,

    @Schema(description = "프로젝트 참여 코드 만료 시간", example = "2025-09-05T16:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    LocalDateTime expiresAt

) {

    public static ProjectJoinCodeResponseDto from(ProjectJoinCode projectJoinCode) {
        return new ProjectJoinCodeResponseDto(
            projectJoinCode.getJoinCode(),
            projectJoinCode.getExpiresAt()
        );
    }

}
