package knu.team1.be.boost.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.UUID;
import knu.team1.be.boost.common.exception.UserNotFoundException;
import knu.team1.be.boost.user.dto.UserResponseDto;
import knu.team1.be.boost.user.dto.UserUpdateRequestDto;
import knu.team1.be.boost.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = UserController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class
)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    // 테스트용 임시 ID
    private final UUID testUserId = UUID.fromString("a1b2c3d4-e5f6-7890-1234-567890abcdef");

    @Test
    @DisplayName("내 정보 조회 API 성공")
    void getMyInfo_Success() throws Exception {
        // given
        UserResponseDto responseDto = new UserResponseDto(
            testUserId,
            "테스트 유저",
            "🤖",
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        given(userService.getUserInfo(any(UUID.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/users/me"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("테스트 유저"))
            .andExpect(jsonPath("$.profileEmoji").value("🤖"));
    }

    @Test
    @DisplayName("내 정보 조회 API 실패 - 존재하지 않는 사용자")
    void getMyInfo_Fail_UserNotFound() throws Exception {
        // given
        given(userService.getUserInfo(any(UUID.class)))
            .willThrow(new UserNotFoundException(testUserId));

        // when & then
        mockMvc.perform(get("/api/users/me"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("내 정보 수정 API 성공")
    void updateMyInfo_Success() throws Exception {
        // given
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto("수정된 이름", "😎");
        UserResponseDto responseDto = new UserResponseDto(
            testUserId,
            "수정된 이름",
            "😎",
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        given(userService.updateUserInfo(any(UUID.class),
            any(UserUpdateRequestDto.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(patch("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("수정된 이름"))
            .andExpect(jsonPath("$.profileEmoji").value("😎"));
    }

    @Test
    @DisplayName("내 정보 수정 API 실패 - 존재하지 않는 사용자")
    void updateMyInfo_Fail_UserNotFound() throws Exception {
        // given
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto("수정된 이름", "😎");
        given(userService.updateUserInfo(any(UUID.class), any(UserUpdateRequestDto.class)))
            .willThrow(new UserNotFoundException(testUserId));

        // when & then
        mockMvc.perform(patch("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("회원 탈퇴 API 성공")
    void deleteMyAccount_Success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/users/me"))
            .andDo(print())
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("회원 탈퇴 API 실패 - 존재하지 않는 사용자")
    void deleteMyAccount_Fail_UserNotFound() throws Exception {
        // given
        doThrow(new UserNotFoundException(testUserId)).when(userService)
            .deleteUser(any(UUID.class));

        // when & then
        mockMvc.perform(delete("/api/users/me"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }
}

