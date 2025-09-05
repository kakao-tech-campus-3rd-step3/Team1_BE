package knu.team1.be.boost.file.exception;

import java.util.UUID;

public class FileNotReadyException extends RuntimeException {

    public FileNotReadyException(UUID fileId) {
        super("아직 다운로드할 수 없는 상태의 파일입니다: " + fileId);
    }
}
