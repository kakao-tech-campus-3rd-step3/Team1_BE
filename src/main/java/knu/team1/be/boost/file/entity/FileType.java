package knu.team1.be.boost.file.entity;

public enum FileType {
    PDF("application/pdf", "pdf");

    private final String mimeType;
    private final String extension;

    FileType(String mimeType, String extension) {
        this.mimeType = mimeType;
        this.extension = extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getExtension() {
        return extension;
    }

    public static FileType fromContentType(String contentType) {
        for (FileType type : values()) {
            if (type.mimeType.equalsIgnoreCase(contentType)) {
                return type;
            }
        }
        throw new IllegalArgumentException(contentType + " 은 지원하지 않는 파일 타입 입니다!");
    }

}
