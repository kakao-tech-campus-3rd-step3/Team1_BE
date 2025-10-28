package knu.team1.be.boost.file.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.file.entity.File;

@Schema(description = "프로젝트별 파일 목록 응답 DTO")
public record ProjectFileListResponseDto(

    @Schema(description = "프로젝트 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID projectId,

    @ArraySchema(
        schema = @Schema(implementation = FileResponseDto.class),
        arraySchema = @Schema(description = "프로젝트에 속한 파일 목록")
    )
    List<FileResponseDto> files,

    @Schema(description = "응답에 포함된 파일 개수", example = "10")
    int count,

    @Schema(description = "다음 페이지 요청 시 사용할 커서 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID nextCursor,

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    boolean hasNext

) {

    public static ProjectFileListResponseDto from(
        UUID projectId,
        List<File> files,
        int limit
    ) {
        boolean hasNext = files.size() > limit;
        UUID nextCursor = null;

        List<FileResponseDto> fileDtos = files.stream()
            .limit(limit)
            .map(FileResponseDto::from)
            .toList();

        if (hasNext && !fileDtos.isEmpty()) {
            nextCursor = fileDtos.getLast().id();
        }

        return new ProjectFileListResponseDto(
            projectId,
            fileDtos,
            fileDtos.size(),
            nextCursor,
            hasNext
        );
    }
}
