package knu.team1.be.boost.file.exception;

public class FileTooLargeException extends RuntimeException {

    public FileTooLargeException(long sizeBytes, long maxBytes) {
        super("파일 크기가 허용된 최대 용량을 초과했습니다. 요청 크기: "
            + toMB(sizeBytes) + "MB / 최대 허용: " + toMB(maxBytes) + "MB");
    }

    private static String toMB(long bytes) {
        double mb = bytes / 1024.0 / 1024.0;
        return String.format("%.2f", mb);
    }
}
