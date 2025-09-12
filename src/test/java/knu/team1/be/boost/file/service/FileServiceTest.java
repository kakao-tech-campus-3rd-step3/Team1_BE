package knu.team1.be.boost.file.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import knu.team1.be.boost.file.dto.FileCompleteRequestDto;
import knu.team1.be.boost.file.dto.FileCompleteResponseDto;
import knu.team1.be.boost.file.dto.FileRequestDto;
import knu.team1.be.boost.file.dto.FileResponseDto;
import knu.team1.be.boost.file.entity.File;
import knu.team1.be.boost.file.entity.FileStatus;
import knu.team1.be.boost.file.entity.FileType;
import knu.team1.be.boost.file.entity.vo.FileMetadata;
import knu.team1.be.boost.file.entity.vo.StorageKey;
import knu.team1.be.boost.file.exception.FileAlreadyUploadCompletedException;
import knu.team1.be.boost.file.exception.FileNotFoundException;
import knu.team1.be.boost.file.exception.FileNotReadyException;
import knu.team1.be.boost.file.exception.FileTooLargeException;
import knu.team1.be.boost.file.infra.s3.PresignedUrlFactory;
import knu.team1.be.boost.file.repository.FileRepository;
import knu.team1.be.boost.task.entity.Task;
import knu.team1.be.boost.task.exception.TaskNotFoundException;
import knu.team1.be.boost.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.unit.DataSize;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    FileRepository fileRepository;
    @Mock
    TaskRepository taskRepository;
    @Mock
    PresignedUrlFactory presignedUrlFactory;

    FileService fileService;

    @BeforeEach
    void setUp() {
        fileService = new FileService(fileRepository, taskRepository, presignedUrlFactory);
        ReflectionTestUtils.setField(fileService, "bucket", "test-bucket");
        ReflectionTestUtils.setField(fileService, "expireSeconds", 900);
        ReflectionTestUtils.setField(fileService, "maxUploadSize", DataSize.ofMegabytes(5));
    }


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

            given(fileRepository.save(any(File.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            PresignedPutObjectRequest putReq = mock(PresignedPutObjectRequest.class);
            when(putReq.url()).thenReturn(
                new URL("https://boost-s3-bucket-storage.s3.ap-northeast-2.amazonaws.com/..."));
            given(presignedUrlFactory.forUpload(anyString(), anyString(), anyString(), anyInt()))
                .willReturn(putReq);

            // when
            FileResponseDto response = fileService.uploadFile(request);

            // then
            assertThat(response.key()).startsWith("file/");
            assertThat(response.key()).endsWith(".pdf");
            assertThat(response.url()).isEqualTo(
                "https://boost-s3-bucket-storage.s3.ap-northeast-2.amazonaws.com/...");
            assertThat(response.method()).isEqualTo("PUT");
            assertThat(response.headers().get("Content-Type")).isEqualTo("application/pdf");
            assertThat(response.headers().get("x-amz-server-side-encryption")).isEqualTo("AES256");
            assertThat(response.expiresInSeconds()).isEqualTo(900);

            ArgumentCaptor<File> savedCap = ArgumentCaptor.forClass(File.class);
            verify(fileRepository).save(savedCap.capture());
            File saved = savedCap.getValue();
            assertThat(saved.getMetadata().originalFilename()).isEqualTo("최종 보고서.pdf");
            assertThat(saved.getMetadata().contentType()).isEqualTo("application/pdf");
            assertThat(saved.getMetadata().sizeBytes()).isEqualTo(1048576);
            assertThat(saved.getType()).isEqualTo(FileType.PDF);
            assertThat(saved.getStatus()).isEqualTo(FileStatus.PENDING);
            assertThat(saved.getStorageKey().value()).isEqualTo(response.key());
        }

        @Test
        @DisplayName("업로드 Pre-Signed URL 발급 실패 - 413 (파일 크기 제한 5MB 초과)")
        void test2() {
            FileRequestDto request = new FileRequestDto(
                "최종 보고서.pdf",
                "application/pdf",
                500000000
            );

            // when & then
            assertThatThrownBy(() -> fileService.uploadFile(request))
                .isInstanceOf(FileTooLargeException.class);

            verifyNoInteractions(fileRepository, presignedUrlFactory);
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

            File file = File.builder()
                .id(fileId)
                .metadata(FileMetadata.of(
                    "최종 보고서.pdf",
                    "application/pdf",
                    1048576)
                )
                .type(FileType.PDF)
                .storageKey(new StorageKey("file/2025/09/10/" + fileId + ".pdf"))
                .status(FileStatus.COMPLETED)
                .build();

            given(fileRepository.findById(fileId)).willReturn(Optional.of(file));

            PresignedGetObjectRequest getReq = mock(PresignedGetObjectRequest.class);
            when(getReq.url()).thenReturn(
                new URL("https://boost-s3-bucket-storage.s3.ap-northeast-2.amazonaws.com/..."));
            given(presignedUrlFactory.forDownload(
                anyString(), anyString(), anyString(), anyString(), anyInt()
            )).willReturn(getReq);

            // when
            FileResponseDto response = fileService.downloadFile(fileId);

            // then
            assertThat(response.fileId()).isEqualTo(fileId);
            assertThat(response.key()).isEqualTo(file.getStorageKey().value());
            assertThat(response.method()).isEqualTo("GET");
            assertThat(response.url()).isEqualTo(
                "https://boost-s3-bucket-storage.s3.ap-northeast-2.amazonaws.com/...");
            assertThat(response.expiresInSeconds()).isEqualTo(900);
        }

        @Test
        @DisplayName("다운로드 Pre-Signed URL 발급 실패 - 404 (파일 존재 x)")
        void test2() {
            // given
            UUID fileId = UUID.randomUUID();
            given(fileRepository.findById(fileId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> fileService.downloadFile(fileId))
                .isInstanceOf(FileNotFoundException.class);

            verifyNoInteractions(presignedUrlFactory);
        }

        @Test
        @DisplayName("다운로드 Pre-Signed URL 발급 실패 - 409 (업로드 미완료)")
        void test3() {
            // given
            UUID fileId = UUID.randomUUID();
            File file = File.builder()
                .id(fileId)
                .metadata(FileMetadata.of(
                    "최종 보고서.pdf",
                    "application/pdf",
                    1048576)
                )
                .type(FileType.PDF)
                .storageKey(new StorageKey("file/2025/09/10/" + fileId + ".pdf"))
                .status(FileStatus.PENDING)
                .build();
            given(fileRepository.findById(fileId)).willReturn(Optional.of(file));

            // when & then
            assertThatThrownBy(() -> fileService.downloadFile(fileId))
                .isInstanceOf(FileNotReadyException.class);

            verifyNoInteractions(presignedUrlFactory);
        }

    }

    @Nested
    @DisplayName("업로드 완료 요청")
    class CompleteUpload {

        @Test
        @DisplayName("업로드 완료 요청 성공")
        void test1() {
            // given
            UUID fileId = UUID.randomUUID();
            UUID taskId = UUID.randomUUID();
            File file = File.builder()
                .id(fileId)
                .metadata(FileMetadata.of(
                    "최종 보고서.pdf",
                    "application/pdf",
                    1048576)
                )
                .type(FileType.PDF)
                .storageKey(new StorageKey("file/2025/09/10/" + fileId + ".pdf"))
                .status(FileStatus.PENDING)
                .build();

            Task task = Task.builder()
                .id(taskId)
                .build();

            given(fileRepository.findById(fileId)).willReturn(Optional.of(file));
            given(taskRepository.findById(taskId)).willReturn(Optional.of(task));

            FileCompleteRequestDto request = new FileCompleteRequestDto(
                taskId,
                "최종 보고서.pdf",
                "application/pdf",
                1048576
            );

            // when
            FileCompleteResponseDto response = fileService.completeUpload(fileId, request);

            // then
            assertThat(response.fileId()).isEqualTo(fileId);
            assertThat(file.getStatus()).isEqualTo(FileStatus.COMPLETED);
            assertThat(file.getTask()).isEqualTo(task);
        }

        @Test
        @DisplayName("업로드 완료 요청 실패 - 400 (파일 메타 데이터 불일치)")
        void test2() {
            // given
            UUID fileId = UUID.randomUUID();
            UUID taskId = UUID.randomUUID();
            File file = File.builder()
                .id(fileId)
                .metadata(FileMetadata.of(
                    "최종 보고서.pdf",
                    "application/pdf",
                    1048576)
                )
                .type(FileType.PDF)
                .storageKey(new StorageKey("file/2025/09/10/" + fileId + ".pdf"))
                .status(FileStatus.PENDING)
                .build();

            given(fileRepository.findById(fileId)).willReturn(Optional.of(file));

            FileCompleteRequestDto req = new FileCompleteRequestDto(
                taskId, "다른 파일.pdf", "application/pdf", 1234
            );

            // when & then
            assertThatThrownBy(() -> fileService.completeUpload(fileId, req))
                .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(taskRepository);
        }

        @Test
        @DisplayName("업로드 완료 요청 실패 - 404 (파일 존재 x)")
        void test3() {
            // given
            UUID fileId = UUID.randomUUID();
            UUID taskId = UUID.randomUUID();
            given(fileRepository.findById(fileId)).willReturn(Optional.empty());

            FileCompleteRequestDto request = new FileCompleteRequestDto(
                taskId,
                "최종 보고서.pdf",
                "application/pdf",
                1048576
            );

            // when & then
            assertThatThrownBy(() -> fileService.completeUpload(fileId, request))
                .isInstanceOf(FileNotFoundException.class);

            verifyNoInteractions(taskRepository);
        }

        @Test
        @DisplayName("업로드 완료 요청 실패 - 404 (할 일 존재 x)")
        void test4() {
            // given
            UUID fileId = UUID.randomUUID();
            UUID taskId = UUID.randomUUID();
            File file = File.builder()
                .id(fileId)
                .metadata(FileMetadata.of(
                    "최종 보고서.pdf",
                    "application/pdf",
                    1048576)
                )
                .type(FileType.PDF)
                .storageKey(new StorageKey("file/2025/09/10/" + fileId + ".pdf"))
                .status(FileStatus.PENDING)
                .build();

            given(fileRepository.findById(fileId)).willReturn(Optional.of(file));
            given(taskRepository.findById(taskId)).willReturn(Optional.empty());

            FileCompleteRequestDto request = new FileCompleteRequestDto(
                taskId,
                "최종 보고서.pdf",
                "application/pdf",
                1048576
            );

            // when & then
            assertThatThrownBy(() -> fileService.completeUpload(fileId, request))
                .isInstanceOf(TaskNotFoundException.class);
        }

        @Test
        @DisplayName("업로드 완료 요청 실패 - 409 (이미 업로드 완료된 파일)")
        void test5() {
            // given
            UUID fileId = UUID.randomUUID();
            UUID taskId = UUID.randomUUID();
            File file = File.builder()
                .id(fileId)
                .metadata(FileMetadata.of(
                    "최종 보고서.pdf",
                    "application/pdf",
                    1048576)
                )
                .type(FileType.PDF)
                .storageKey(new StorageKey("file/2025/09/10/" + fileId + ".pdf"))
                .status(FileStatus.COMPLETED)
                .build();

            given(fileRepository.findById(fileId)).willReturn(Optional.of(file));

            FileCompleteRequestDto request = new FileCompleteRequestDto(
                taskId,
                "최종 보고서.pdf",
                "application/pdf",
                1048576
            );

            // when & then
            assertThatThrownBy(() -> fileService.completeUpload(fileId, request))
                .isInstanceOf(FileAlreadyUploadCompletedException.class);

            verifyNoInteractions(taskRepository);
        }

    }

}
