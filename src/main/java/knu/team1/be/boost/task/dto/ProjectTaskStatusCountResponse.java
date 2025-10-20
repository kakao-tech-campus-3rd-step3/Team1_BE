package knu.team1.be.boost.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "프로젝트 기준 상태별 할 일 수 응답 DTO")
public record ProjectTaskStatusCountResponse(

    @Schema(description = "프로젝트 ID", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    UUID projectId,

    @Schema(description = "TODO 상태의 할 일 수", example = "5")
    int todo,

    @Schema(description = "진행 중(PROGRESS) 상태의 할 일 수", example = "3")
    int progress,

    @Schema(description = "검토 중(REVIEW) 상태의 할 일 수", example = "2")
    int review,

    @Schema(description = "완료(DONE) 상태의 할 일 수", example = "7")
    int done
) {

    public static ProjectTaskStatusCountResponse from(
        UUID projectId,
        int todo,
        int progress,
        int review,
        int done
    ) {
        return new ProjectTaskStatusCountResponse(
            projectId,
            todo,
            progress,
            review,
            done
        );
    }
}
