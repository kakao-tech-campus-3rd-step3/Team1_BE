package knu.team1.be.boost.user.service;

import java.util.UUID;
import knu.team1.be.boost.user.dto.UserResponseDto;
import knu.team1.be.boost.user.dto.UserUpdateRequestDto;

public interface UserService {

    UserResponseDto getUserInfo(UUID userId);

    UserResponseDto updateUserInfo(UUID userId, UserUpdateRequestDto requestDto);

    void deleteUser(UUID userId);
}
