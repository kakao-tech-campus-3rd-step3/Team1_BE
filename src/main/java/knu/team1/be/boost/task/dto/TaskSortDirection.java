package knu.team1.be.boost.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "정렬 방향")
public enum TaskSortDirection {
    @Schema(description = "오름차순")
    ASC,
    @Schema(description = "내림차순")
    DESC
}
