package knu.team1.be.boost.file.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import knu.team1.be.boost.file.dto.FileCompleteRequestDto;
import knu.team1.be.boost.file.dto.FileCompleteResponseDto;
import knu.team1.be.boost.file.dto.FileRequestDto;
import knu.team1.be.boost.file.dto.FileResponseDto;
import knu.team1.be.boost.file.service.FileService;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = FileController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class
)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FileService fileService;

    @Nested
    @DisplayName("업로드 Pre-Signed URL 발급")
    class UploadPresign {

        @Test
        @DisplayName("업로드 Pre-Signed URL 발급 성공")
        void test1() throws Exception {
            // given
            FileRequestDto request = new FileRequestDto(
                "최종 보고서.pdf",
                "application/pdf",
                1048576
            );

            UUID fileId = UUID.randomUUID();
            FileResponseDto response = new FileResponseDto(
                fileId,
                "file/2025/09/09/" + fileId + ".pdf",
                "https://boost-s3-bucket-storage.s3.ap-northeast-2.amazonaws.com/...",
                "PUT",
                Map.of("Content-Type", "application/pdf",
                    "x-amz-server-side-encryption", "AES256"),
                900
            );

            given(fileService.uploadFile(any(FileRequestDto.class)))
                .willReturn(response);

            // when & then
            mockMvc.perform(
                    post("/api/files/upload-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/files/" + fileId))
                .andExpect(jsonPath("$.fileId").value(fileId.toString()))
                .andExpect(jsonPath("$.key").value("file/2025/09/09/" + fileId + ".pdf"))
                .andExpect(jsonPath("$.url").value(
                    "https://boost-s3-bucket-storage.s3.ap-northeast-2.amazonaws.com/..."))
                .andExpect(jsonPath("$.method").value("PUT"))
                .andExpect(jsonPath("$.headers['Content-Type']").value("application/pdf"))
                .andExpect(jsonPath("$.expiresInSeconds").value(900));

        }

        @Test
        @DisplayName("업로드 Pre-Signed URL 발급 실패 - 400 (빈 파일명)")
        void test2() throws Exception {
            FileRequestDto request = new FileRequestDto(
                "",
                "application/pdf",
                1048576
            );

            // when & then
            mockMvc.perform(
                    post("/api/files/upload-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("업로드 Pre-Signed URL 발급 실패 - 413 (파일 크기 제한 초과)")
        void test3() throws Exception {
            // given
            FileRequestDto request = new FileRequestDto(
                "최종 보고서.pdf",
                "application/pdf",
                1048576
            );
            given(fileService.uploadFile(any(FileRequestDto.class)))
                .willThrow(new BusinessException(ErrorCode.FILE_TOO_LARGE));

            // when & then
            mockMvc.perform(
                    post("/api/files/upload-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isPayloadTooLarge());
        }

    }

    @Nested
    @DisplayName("다운로드 Pre-Signed URL 발급")
    class DownloadPresign {

        @Test
        @DisplayName("다운로드 Pre-Signed URL 발급 성공")
        void test1() throws Exception {
            // given
            UUID fileId = UUID.randomUUID();
            FileResponseDto response = new FileResponseDto(
                fileId,
                "file/2025/09/09/" + fileId + ".pdf",
                "https://boost-s3-bucket-storage.s3.ap-northeast-2.amazonaws.com/...",
                "GET",
                Collections.emptyMap(),
                900
            );

            given(fileService.downloadFile(eq(fileId)))
                .willReturn(response);

            // when & then
            mockMvc.perform(get("/api/files/{fileId}/download-url", fileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileId").value(fileId.toString()))
                .andExpect(jsonPath("$.key").value("file/2025/09/09/" + fileId + ".pdf"))
                .andExpect(jsonPath("$.url").value(
                    "https://boost-s3-bucket-storage.s3.ap-northeast-2.amazonaws.com/..."))
                .andExpect(jsonPath("$.method").value("GET"))
                .andExpect(jsonPath("$.expiresInSeconds").value(900));
        }

        @Test
        @DisplayName("다운로드 Pre-Signed URL 발급 실패 - 404 (파일 존재 x)")
        void test2() throws Exception {
            // given
            UUID fileId = UUID.randomUUID();
            given(fileService.downloadFile(eq(fileId)))
                .willThrow(new BusinessException(ErrorCode.FILE_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/files/{fileId}/download-url", fileId))
                .andExpect(status().isNotFound());
        }

    }


    @Nested
    @DisplayName("업로드 완료 요청")
    class CompleteUpload {

        @Test
        @DisplayName("업로드 완료 요청 성공")
        void test3() throws Exception {
            UUID fileId = UUID.randomUUID();
            UUID taskId = UUID.randomUUID();

            FileCompleteRequestDto request =
                new FileCompleteRequestDto(
                    taskId,
                    "최종 보고서.pdf",
                    "application/pdf",
                    1048576
                );

            FileCompleteResponseDto response =
                new FileCompleteResponseDto(
                    fileId,
                    taskId,
                    "completed",
                    LocalDateTime.of(2025, 9, 10, 12, 34, 56)
                );

            given(fileService.completeUpload(eq(fileId), any(FileCompleteRequestDto.class)))
                .willReturn(response);

            // when & then
            mockMvc.perform(patch("/api/files/{fileId}/complete", fileId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileId").value(fileId.toString()))
                .andExpect(jsonPath("$.taskId").value(taskId.toString()))
                .andExpect(jsonPath("$.status").value("completed"))
                .andExpect(jsonPath("$.completedAt").value("2025-09-10T12:34:56"));
        }


        @Test
        @DisplayName("업로드 완료 요청 실패 - 400 (빈 파일명)")
        void test8() throws Exception {
            // given
            UUID fileId = UUID.randomUUID();
            UUID taskId = UUID.randomUUID();
            FileCompleteRequestDto request = new FileCompleteRequestDto(
                taskId,
                "",
                "application/pdf",
                1048576
            );

            // when & then
            mockMvc.perform(
                    patch("/api/files/{fileId}/complete", fileId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("업로드 완료 요청 실패 - 409 (이미 존재하는 파일)")
        void test9() throws Exception {
            // given
            UUID fileId = UUID.randomUUID();
            UUID taskId = UUID.randomUUID();
            FileCompleteRequestDto request =
                new FileCompleteRequestDto(
                    taskId,
                    "최종 보고서.pdf",
                    "application/pdf",
                    1048576
                );

            given(fileService.completeUpload(eq(fileId), any(FileCompleteRequestDto.class)))
                .willThrow(new BusinessException(ErrorCode.FILE_ALREADY_UPLOAD_COMPLETED));

            // when & then
            mockMvc.perform(
                    patch("/api/files/{fileId}/complete", fileId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict());
        }

    }

}
