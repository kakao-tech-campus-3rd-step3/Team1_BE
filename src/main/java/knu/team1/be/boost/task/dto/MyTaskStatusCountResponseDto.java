package knu.team1.be.boost.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "나의 할 일 기준 상태별 할 일 수 응답 DTO")
public record MyTaskStatusCountResponseDto(

    @Schema(description = "사용자 ID", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    UUID memberId,

    @Schema(description = "TODO 상태의 할 일 수", example = "5")
    long todo,

    @Schema(description = "진행 중(PROGRESS) 상태의 할 일 수", example = "3")
    long progress,

    @Schema(description = "검토 중(REVIEW) 상태의 할 일 수", example = "2")
    long review,

    @Schema(description = "완료(DONE) 상태의 할 일 수", example = "7")
    long done
) {

    public static MyTaskStatusCountResponseDto from(
        UUID memberId,
        long todo,
        long progress,
        long review,
        long done
    ) {
        return new MyTaskStatusCountResponseDto(
            memberId,
            todo,
            progress,
            review,
            done
        );
    }
}
