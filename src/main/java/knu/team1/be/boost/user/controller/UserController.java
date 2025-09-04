package knu.team1.be.boost.user.controller;

import java.util.UUID;
import knu.team1.be.boost.user.dto.UserResponseDto;
import knu.team1.be.boost.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyInfo() {
        UUID userId = UUID.fromString(
            "a1b2c3d4-e5f6-7890-1234-567890abcdef"); // 테스트용 userId 임의 생성 -> 카카오 로그인 구현
        UserResponseDto userInfo = userService.getUserInfo(userId);
        return ResponseEntity.ok(userInfo);
    }
}
