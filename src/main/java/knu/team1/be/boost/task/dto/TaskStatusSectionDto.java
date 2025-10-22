package knu.team1.be.boost.task.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import knu.team1.be.boost.task.entity.Task;

@Schema(description = "커서 기반 상태 리스트 응답")
public record TaskStatusSectionDto(
    @ArraySchema(
        schema = @Schema(implementation = TaskResponseDto.class),
        arraySchema = @Schema(description = "Task 목록")
    )
    List<TaskResponseDto> tasks,

    @Schema(description = "현재 응답에 포함된 Task 개수", example = "20")
    Integer count,

    @Schema(description = "다음 페이지 호출용 커서", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID nextCursor,

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    Boolean hasNext
) {

    public static TaskStatusSectionDto from(
        List<Task> tasks,
        int limit,
        Map<UUID, Integer> fileCountMap,
        Map<UUID, Integer> commentCountMap
    ) {
        Map<UUID, Integer> safeFileCountMap =
            fileCountMap == null ? Collections.emptyMap() : fileCountMap;
        Map<UUID, Integer> safeCommentCountMap =
            commentCountMap == null ? Collections.emptyMap() : commentCountMap;

        boolean hasNext = tasks.size() > limit;
        UUID nextCursor = null;

        List<TaskResponseDto> taskResponseDtos = tasks.stream()
            .limit(limit)
            .map(task -> {
                int fileCount = safeFileCountMap.getOrDefault(task.getId(), 0);
                int commentCount = safeCommentCountMap.getOrDefault(task.getId(), 0);
                return TaskResponseDto.from(task, fileCount, commentCount);
            })
            .toList();

        if (hasNext && !taskResponseDtos.isEmpty()) {
            nextCursor = taskResponseDtos.getLast().taskId();
        }

        return new TaskStatusSectionDto(
            taskResponseDtos,
            taskResponseDtos.size(),
            nextCursor,
            hasNext
        );
    }
}
