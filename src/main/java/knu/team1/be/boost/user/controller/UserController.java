package knu.team1.be.boost.user.controller;

import java.util.UUID;
import knu.team1.be.boost.user.dto.UserResponseDto;
import knu.team1.be.boost.user.dto.UserUpdateRequestDto;
import knu.team1.be.boost.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    private final UUID userId = UUID.fromString(
        "a1b2c3d4-e5f6-7890-1234-567890abcdef"); // 테스트용 userId 임의 생성 -> 카카오 로그인 구현

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyInfo() {
        UserResponseDto userInfo = userService.getUserInfo(userId);
        return ResponseEntity.ok(userInfo);
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponseDto> updateMyInfo(
        @RequestBody UserUpdateRequestDto requestDto) {
        UserResponseDto updatedUserInfo = userService.updateUserInfo(userId, requestDto);
        return ResponseEntity.ok(updatedUserInfo);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount() {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
