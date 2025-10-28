package knu.team1.be.boost.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.file.entity.File;

@Schema(description = "프로젝트별 파일 목록 응답 DTO")
public record ProjectFileListResponseDto(

    @Schema(description = "프로젝트 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID projectId,

    @Schema(description = "프로젝트에 속한 파일 목록")
    List<FileResponseDto> files

) {

    public static ProjectFileListResponseDto from(UUID projectId, List<File> files) {
        return new ProjectFileListResponseDto(
            projectId,
            files.stream().map(FileResponseDto::from).toList()
        );
    }
}
