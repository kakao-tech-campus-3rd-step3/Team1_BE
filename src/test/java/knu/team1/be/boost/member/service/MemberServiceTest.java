package knu.team1.be.boost.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.UUID;
import knu.team1.be.boost.common.exception.MemberNotFoundException;
import knu.team1.be.boost.member.dto.MemberResponseDto;
import knu.team1.be.boost.member.dto.MemberUpdateRequestDto;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.member.repository.MemberRepository;
import knu.team1.be.boost.member.vo.OauthInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @InjectMocks
    private MemberServiceImpl userService;

    @Mock
    private MemberRepository memberRepository;

    // 테스트용 임시 ID 및 데이터
    private final UUID testUserId = UUID.fromString("a1b2c3d4-e5f6-7890-1234-567890abcdef");
    private final OauthInfo testOauthInfo = new OauthInfo("kakao", "123456789");
    private final Member testMember = Member.builder()
        .id(testUserId)
        .name("테스트 유저")
        .profileEmoji("🤖")
        .oauthInfo(testOauthInfo)
        .build();

    @Test
    @DisplayName("회원 정보 조회 성공")
    void getMember_Success() {
        // given
        given(memberRepository.findById(testUserId)).willReturn(Optional.of(testMember));

        // when
        MemberResponseDto responseDto = userService.getMember(testUserId);

        // then
        assertNotNull(responseDto);
        assertEquals("테스트 유저", responseDto.name());
        assertEquals("🤖", responseDto.profileEmoji());
    }

    @Test
    @DisplayName("회원 정보 조회 실패 - 존재하지 않는 회원")
    void getUserInfo_Fail_MemberNotFound() {
        // given
        UUID nonExistentUserId = UUID.randomUUID();
        given(memberRepository.findById(nonExistentUserId)).willReturn(Optional.empty());

        // when & then
        assertThrows(MemberNotFoundException.class, () -> {
            userService.getMember(nonExistentUserId);
        });
    }

    @Test
    @DisplayName("회원 정보 수정 성공")
    void updateMember_Success() {
        // given
        MemberUpdateRequestDto requestDto = new MemberUpdateRequestDto("수정된 이름", "😎");
        given(memberRepository.findById(testUserId)).willReturn(Optional.of(testMember));

        // when
        MemberResponseDto responseDto = userService.updateMember(testUserId, requestDto);

        // then
        assertNotNull(responseDto);
        assertEquals("수정된 이름", responseDto.name());
        assertEquals("😎", responseDto.profileEmoji());
    }

    @Test
    @DisplayName("회원 정보 수정 실패 - 존재하지 않는 회원")
    void updateMemberInfo_Fail_MemberNotFound() {
        // given
        UUID nonExistentUserId = UUID.randomUUID();
        MemberUpdateRequestDto requestDto = new MemberUpdateRequestDto("수정된 이름", "😎");
        given(memberRepository.findById(nonExistentUserId)).willReturn(Optional.empty());

        // when & then
        assertThrows(MemberNotFoundException.class, () -> {
            userService.updateMember(nonExistentUserId, requestDto);
        });
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void deleteMember_Success() {
        // given
        given(memberRepository.findById(testUserId)).willReturn(Optional.of(testMember));

        // when
        userService.deleteMember(testUserId);

        // then
        verify(memberRepository).delete(testMember);
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 존재하지 않는 회원")
    void deleteUser_Fail_MemberNotFound() {
        // given
        UUID nonExistentUserId = UUID.randomUUID();
        given(memberRepository.findById(nonExistentUserId)).willReturn(Optional.empty());

        // when & then
        assertThrows(MemberNotFoundException.class, () -> {
            userService.deleteMember(nonExistentUserId);
        });
    }
}

