package knu.team1.be.boost.file.exception;

import java.util.UUID;

public class FileAlreadyUploadCompletedException extends RuntimeException {

    public FileAlreadyUploadCompletedException(UUID fileId) {
        super("파일이 이미 업로드 완료 상태입니다: " + fileId);
    }
}
