package knu.team1.be.boost.comment.dto;

import java.util.UUID;
import knu.team1.be.boost.comment.entity.vo.FileInfo;

public record FileInfoResponseDto(

    UUID fileId,
    Integer filePage,
    Float fileX,
    Float fileY
) {

    public static FileInfoResponseDto from(FileInfo fileInfo) {
        // 댓글에 파일 정보가 없는 경우 null을 반환
        if (fileInfo == null || fileInfo.getFile() == null) {
            return null;
        }

        return new FileInfoResponseDto(
            fileInfo.getFile().getId(),
            fileInfo.getFilePage(),
            fileInfo.getFileX(),
            fileInfo.getFileY()
        );
    }
}
