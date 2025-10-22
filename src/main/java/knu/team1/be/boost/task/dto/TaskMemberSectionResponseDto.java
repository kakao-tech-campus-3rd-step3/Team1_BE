package knu.team1.be.boost.task.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import knu.team1.be.boost.member.dto.MemberResponseDto;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.task.entity.Task;

@Schema(description = "팀원 섹션 응답 DTO")
public record TaskMemberSectionResponseDto(

    @Schema(description = "팀원 정보")
    MemberResponseDto member,

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

    public static TaskMemberSectionResponseDto from(
        Member member,
        List<Task> tasks,
        int limit,
        Map<UUID, Integer> fileCountMap,
        Map<UUID, Integer> commentCountMap
    ) {
        boolean hasNext = tasks.size() > limit;
        UUID nextCursor = null;

        List<TaskResponseDto> taskResponseDtos = tasks.stream()
            .limit(limit)
            .map(task -> {
                int fileCount = fileCountMap.getOrDefault(task.getId(), 0);
                int commentCount = commentCountMap.getOrDefault(task.getId(), 0);
                return TaskResponseDto.from(task, fileCount, commentCount);
            })
            .toList();

        if (hasNext && !taskResponseDtos.isEmpty()) {
            nextCursor = taskResponseDtos.getLast().taskId();
        }

        return new TaskMemberSectionResponseDto(
            MemberResponseDto.from(member),
            taskResponseDtos,
            taskResponseDtos.size(),
            nextCursor,
            hasNext
        );
    }
}
