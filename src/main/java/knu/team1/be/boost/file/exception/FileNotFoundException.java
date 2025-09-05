package knu.team1.be.boost.file.exception;

import java.util.UUID;

public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException(UUID fileId) {
        super("파일을 찾을 수 없습니다: " + fileId);
    }
}
