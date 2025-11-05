package knu.team1.be.boost.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.projectMembership.entity.ProjectRole;

public record ProjectResponseDto(
    @Schema(description = "프로젝트 고유 ID", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    UUID id,

    @Schema(description = "프로젝트 이름", example = "카테캠 BOOST")
    String name,

    @Schema(description = "프로젝트 생성/수정 시 기본으로 설정되는 리뷰어 인원 수", example = "2")
    Integer defaultReviewerCount,

    @Schema(description = "프로젝트에서 사용자의 역할", example = "MEMBER")
    ProjectRole role,

    @Schema(description = "프로젝트 알림 허용 여부", example = "true")
    boolean isNotificationEnabled
) {

    public static ProjectResponseDto from(Project project, ProjectRole role, boolean isNotificationEnabled) {
        return new ProjectResponseDto(
            project.getId(),
            project.getName(),
            project.getDefaultReviewerCount(),
            role,
            isNotificationEnabled
        );
    }

}
