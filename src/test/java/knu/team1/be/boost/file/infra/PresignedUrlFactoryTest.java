package knu.team1.be.boost.file.infra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;
import knu.team1.be.boost.file.exception.StorageServiceException;
import knu.team1.be.boost.file.infra.s3.PresignedUrlFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@ExtendWith(MockitoExtension.class)
class PresignedUrlFactoryTest {

    @Mock
    private S3Presigner s3Presigner;

    private PresignedUrlFactory factory;

    @BeforeEach
    void setUp() {
        factory = new PresignedUrlFactory(s3Presigner);
    }

    @Nested
    @DisplayName("업로드 Pre-Signed URL 발급")
    class UploadTest {

        @Test
        @DisplayName("업로드 Pre-Signed URL 발급 성공")
        void test1() {
            // given
            PresignedPutObjectRequest presigned = org.mockito.Mockito.mock(
                PresignedPutObjectRequest.class);
            when(s3Presigner.presignPutObject(any(Consumer.class))).thenReturn(presigned);

            // when
            PresignedPutObjectRequest result =
                factory.forUpload(
                    "test-bucket",
                    "test-key",
                    "content/type",
                    900
                );

            // then
            assertSame(presigned, result);
            verify(s3Presigner).presignPutObject(any(Consumer.class));
        }

        @Test
        @DisplayName("업로드 Pre-Signed URL 발급 실패 - 500 (SDK 오류)")
        void test2() {
            // given
            when(s3Presigner.presignPutObject(any(Consumer.class)))
                .thenThrow(SdkClientException.class);

            // when & then
            assertThrows(StorageServiceException.class, () ->
                factory.forUpload(
                    "test-bucket",
                    "test-key",
                    "content/type",
                    900
                )
            );
        }
    }

    @Nested
    @DisplayName("다운로드 Pre-Signed URL 발급")
    class DownloadTest {

        @Test
        @DisplayName("다운로드 Pre-Signed URL 발급 성공")
        void test1() {
            // given
            PresignedGetObjectRequest presigned = org.mockito.Mockito.mock(
                PresignedGetObjectRequest.class);
            when(s3Presigner.presignGetObject(any(Consumer.class))).thenReturn(presigned);

            // when
            PresignedGetObjectRequest result =
                factory.forDownload(
                    "test-bucket",
                    "test-key",
                    "test.pdf",
                    "content/type",
                    900
                );

            // then
            assertSame(presigned, result);
            verify(s3Presigner).presignGetObject(any(Consumer.class));
        }

        @Test
        @DisplayName("다운로드 Pre-Signed URL 발급 실패 - 500 (SDK 오류)")
        void test2() {
            // given
            when(s3Presigner.presignGetObject(any(Consumer.class)))
                .thenThrow(SdkClientException.builder().message("test-exception").build());

            // when & then
            assertThrows(StorageServiceException.class, () ->
                factory.forDownload(
                    "test-bucket",
                    "test-key",
                    "test.pdf",
                    "content/type",
                    900
                )
            );
        }
    }

    @Nested
    @DisplayName("파일명 정규화")
    class SanitizeFilenameTests {

        @Test
        @DisplayName("null 입력 → 기본값 'download' 반환")
        void test1() {
            assertEquals("download", sanitize(null));
        }

        @Test
        @DisplayName("공백/제어문자 입력 → 기본값 'download' 반환")
        void test2() {
            assertEquals("download", sanitize("   \t\n "));
        }

        @Test
        @DisplayName("제어문자 제거 확인")
        void test3() {
            assertEquals("ab", sanitize("a\nb\t"));
        }

        @Test
        @DisplayName("따옴표/세미콜론/백슬래시 처리")
        void test4() {
            assertEquals("a'b", sanitize("a\"b"));
            assertEquals("ab", sanitize("a;b"));
            assertEquals("ab", sanitize("a\\b"));
        }

        @Test
        @DisplayName("경로 구분자 치환 확인")
        void test5() {
            assertEquals("a_b_c", sanitize("a/b:c"));
        }

        @Test
        @DisplayName(".. 반복은 .으로 축약")
        void test6() {
            assertEquals("a.b.c", sanitize("a..b....c"));
        }

        @Test
        @DisplayName("공백 정리 및 trim")
        void test7() {
            assertEquals("a b", sanitize("   a   b  "));
        }

        @Test
        @DisplayName("최대 길이 255 제한")
        void test8() {
            String longName = "x".repeat(300);
            String out = sanitize(longName);
            assertEquals(255, out.length());
        }
    }

    private static String sanitize(String name) {
        return ReflectionTestUtils.invokeMethod(
            PresignedUrlFactory.class,
            "sanitizeFilename",
            name
        );
    }
}
