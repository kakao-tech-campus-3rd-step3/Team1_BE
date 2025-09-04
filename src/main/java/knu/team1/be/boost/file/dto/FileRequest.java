package knu.team1.be.boost.file.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FileRequest(

    @NotBlank(message = "파일명은 필수입니다.")
    String filename,

    @NotBlank(message = "Content-Type은 필수입니다.")
    String contentType,

    @NotNull(message = "파일 크기는 필수입니다.")
    @Positive(message = "파일 크기는 양수이어야 합니다.")
    Integer sizeBytes

) {

}
