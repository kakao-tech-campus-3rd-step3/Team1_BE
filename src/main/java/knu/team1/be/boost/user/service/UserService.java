package knu.team1.be.boost.user.service;

import java.util.UUID;
import knu.team1.be.boost.user.dto.UserResponseDto;

public interface UserService {

    UserResponseDto getUserInfo(UUID userId);
}
