package knu.team1.be.boost.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "팀원 기준 상태별 할 일 수 응답 DTO (DONE 제외)")
public record MemberTaskStatusCountResponseDto(

    @Schema(description = "프로젝트 ID", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    UUID projectId,

    @Schema(description = "팀원 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID memberId,

    @Schema(description = "TODO 상태의 할 일 수", example = "4")
    long todo,

    @Schema(description = "진행 중(PROGRESS) 상태의 할 일 수", example = "3")
    long progress,

    @Schema(description = "검토 중(REVIEW) 상태의 할 일 수", example = "2")
    long review
) {

    public static MemberTaskStatusCountResponseDto from(
        UUID projectId,
        UUID memberId,
        long todo,
        long progress,
        long review
    ) {
        return new MemberTaskStatusCountResponseDto(
            projectId,
            memberId,
            todo,
            progress,
            review
        );
    }
}
