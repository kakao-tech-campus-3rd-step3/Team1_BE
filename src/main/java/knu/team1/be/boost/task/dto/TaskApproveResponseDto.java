package knu.team1.be.boost.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.task.entity.Task;

@Schema(description = "할 일 승인 응답 DTO")
public record TaskApproveResponseDto(

    @Schema(description = "할 일 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID taskId,

    @Schema(description = "할 일 상태 (TODO/PROGRESS/REVIEW/DONE)", example = "REVIEW")
    String status,

    @Schema(description = "현재 승인된 리뷰어 수", example = "1")
    Integer approvedCount,

    @Schema(description = "필요 승인 리뷰어 수", example = "2")
    Integer requiredReviewerCount

) {

    public static TaskApproveResponseDto from(Task task, List<Member> projectMembers) {
        return new TaskApproveResponseDto(
            task.getId(),
            task.getStatus().name(),
            task.getApprovers().size(),
            task.getRequiredApprovalsCount(projectMembers)
        );
    }

}
