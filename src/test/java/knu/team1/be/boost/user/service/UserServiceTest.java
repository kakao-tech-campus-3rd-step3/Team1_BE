package knu.team1.be.boost.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.UUID;
import knu.team1.be.boost.common.exception.UserNotFoundException;
import knu.team1.be.boost.user.dto.UserResponseDto;
import knu.team1.be.boost.user.dto.UserUpdateRequestDto;
import knu.team1.be.boost.user.entity.User;
import knu.team1.be.boost.user.repository.UserRepository;
import knu.team1.be.boost.user.vo.OauthInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    // 테스트용 임시 ID 및 데이터
    private final UUID testUserId = UUID.fromString("a1b2c3d4-e5f6-7890-1234-567890abcdef");
    private final OauthInfo testOauthInfo = new OauthInfo("kakao", "123456789");
    private final User testUser = User.builder()
        .id(testUserId)
        .name("테스트 유저")
        .profileEmoji("🤖")
        .oauthInfo(testOauthInfo)
        .build();

    @Test
    @DisplayName("사용자 정보 조회 성공")
    void getUserInfo_Success() {
        // given
        given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));

        // when
        UserResponseDto responseDto = userService.getUserInfo(testUserId);

        // then
        assertNotNull(responseDto);
        assertEquals("테스트 유저", responseDto.name());
        assertEquals("🤖", responseDto.profileEmoji());
    }

    @Test
    @DisplayName("사용자 정보 조회 실패 - 존재하지 않는 사용자")
    void getUserInfo_Fail_UserNotFound() {
        // given
        UUID nonExistentUserId = UUID.randomUUID();
        given(userRepository.findById(nonExistentUserId)).willReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserInfo(nonExistentUserId);
        });
    }

    @Test
    @DisplayName("사용자 정보 수정 성공")
    void updateUserInfo_Success() {
        // given
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto("수정된 이름", "😎");
        given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));

        // when
        UserResponseDto responseDto = userService.updateUserInfo(testUserId, requestDto);

        // then
        assertNotNull(responseDto);
        assertEquals("수정된 이름", responseDto.name());
        assertEquals("😎", responseDto.profileEmoji());
    }

    @Test
    @DisplayName("사용자 정보 수정 실패 - 존재하지 않는 사용자")
    void updateUserInfo_Fail_UserNotFound() {
        // given
        UUID nonExistentUserId = UUID.randomUUID();
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto("수정된 이름", "😎");
        given(userRepository.findById(nonExistentUserId)).willReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class, () -> {
            userService.updateUserInfo(nonExistentUserId, requestDto);
        });
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void deleteUser_Success() {
        // given
        given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));

        // when
        userService.deleteUser(testUserId);

        // then
        verify(userRepository).delete(testUser);
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 존재하지 않는 사용자")
    void deleteUser_Fail_UserNotFound() {
        // given
        UUID nonExistentUserId = UUID.randomUUID();
        given(userRepository.findById(nonExistentUserId)).willReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUser(nonExistentUserId);
        });
    }
}

