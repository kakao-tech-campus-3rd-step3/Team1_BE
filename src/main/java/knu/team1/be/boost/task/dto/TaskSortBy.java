package knu.team1.be.boost.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "정렬 기준")
public enum TaskSortBy {
    @Schema(description = "생성일")
    CREATED_AT,
    @Schema(description = "마감일")
    DUE_DATE
}
