package knu.team1.be.boost.user.service;

import java.util.UUID;
import knu.team1.be.boost.common.exception.UserNotFoundException;
import knu.team1.be.boost.user.dto.UserResponseDto;
import knu.team1.be.boost.user.dto.UserUpdateRequestDto;
import knu.team1.be.boost.user.entity.User;
import knu.team1.be.boost.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponseDto getUserInfo(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        return UserResponseDto.from(user);
    }

    @Transactional
    @Override
    public UserResponseDto updateUserInfo(UUID userId, UserUpdateRequestDto requestDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        user.updateUser(requestDto.name(), requestDto.profileEmoji());
        return UserResponseDto.from(user);
    }
}
