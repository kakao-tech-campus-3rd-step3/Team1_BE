package knu.team1.be.boost.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로젝트 파일 요약 응답 DTO - 프로젝트 내 총 파일 개수와 총 용량(바이트) 정보를 담습니다.")
public record ProjectFileSummaryResponseDto(

    @Schema(description = "총 파일 개수", example = "42")
    long totalCount,

    @Schema(description = "총 파일 용량 (바이트 단위)", example = "104857600")
    long totalSizeBytes

) {

    public static ProjectFileSummaryResponseDto from(long totalCount, long totalSizeBytes) {
        return new ProjectFileSummaryResponseDto(
            totalCount,
            totalSizeBytes
        );
    }
}
