package knu.team1.be.boost.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.UUID;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.member.dto.MemberAvatarUpdateRequestDto;
import knu.team1.be.boost.member.dto.MemberNameUpdateRequestDto;
import knu.team1.be.boost.member.dto.MemberResponseDto;
import knu.team1.be.boost.member.service.MemberService;
import knu.team1.be.boost.security.filter.JwtAuthFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = MemberController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthFilter.class
    )
)
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @Autowired
    private ObjectMapper objectMapper;

    // 테스트용 임시 ID
    private final UUID testMemberId = UUID.fromString("a1b2c3d4-e5f6-7890-1234-567890abcdef");

    @Test
    @DisplayName("내 정보 조회 API 성공")
    void getMyInfo_Success() throws Exception {
        // given
        MemberResponseDto responseDto = new MemberResponseDto(
            testMemberId,
            "테스트 유저",
            "1111",
            "#FF5733",
            true,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        given(memberService.getMember(any())).willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/members/me"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("테스트 유저"))
            .andExpect(jsonPath("$.avatar").value("1111"));
    }

    @Test
    @DisplayName("내 정보 조회 API 실패 - 존재하지 않는 회원")
    void getMyInfo_Fail_MemberNotFound() throws Exception {
        // given
        given(memberService.getMember(any()))
            .willThrow(new BusinessException(
                ErrorCode.MEMBER_NOT_FOUND,
                "memberId: " + testMemberId
            ));

        // when & then
        mockMvc.perform(get("/api/members/me"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("내 이름 수정 API 성공")
    void updateMyName_Success() throws Exception {
        // given
        MemberNameUpdateRequestDto requestDto = new MemberNameUpdateRequestDto("수정된 이름");
        MemberResponseDto responseDto = new MemberResponseDto(
            testMemberId,
            "수정된 이름",
            "1111",
            "#FF5733",
            true,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        given(memberService.updateMemberName(any(),
            any(MemberNameUpdateRequestDto.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(put("/api/members/me/name")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("수정된 이름"));
    }

    @Test
    @DisplayName("내 이름 수정 API 실패 - 존재하지 않는 회원")
    void updateMyName_Fail_MemberNotFound() throws Exception {
        // given
        MemberNameUpdateRequestDto requestDto = new MemberNameUpdateRequestDto("수정된 이름");
        given(memberService.updateMemberName(any(),
            any(MemberNameUpdateRequestDto.class)))
            .willThrow(new BusinessException(
                ErrorCode.MEMBER_NOT_FOUND,
                "memberId: " + testMemberId
            ));

        // when & then
        mockMvc.perform(put("/api/members/me/name")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("내 아바타 수정 API 성공")
    void updateMyAvatar_Success() throws Exception {
        // given
        MemberAvatarUpdateRequestDto requestDto = new MemberAvatarUpdateRequestDto("1111",
            "#FF5733");
        MemberResponseDto responseDto = new MemberResponseDto(
            testMemberId,
            "원래 이름",
            "1112",
            "#FF5733",
            true,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        given(memberService.updateMemberAvatar(any(),
            any(MemberAvatarUpdateRequestDto.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(put("/api/members/me/avatar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("원래 이름"))
            .andExpect(jsonPath("$.avatar").value("1112"));
    }

    @Test
    @DisplayName("내 아바타 수정 API 실패 - 존재하지 않는 회원")
    void updateMyAvatar_Fail_MemberNotFound() throws Exception {
        // given
        MemberAvatarUpdateRequestDto requestDto = new MemberAvatarUpdateRequestDto("1112",
            "#FF5733");
        given(memberService.updateMemberAvatar(any(),
            any(MemberAvatarUpdateRequestDto.class)))
            .willThrow(new BusinessException(
                ErrorCode.MEMBER_NOT_FOUND,
                "memberId: " + testMemberId
            ));

        // when & then
        mockMvc.perform(put("/api/members/me/avatar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("회원 탈퇴 API 성공")
    void deleteMyAccount_Success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/members/me"))
            .andDo(print())
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("회원 탈퇴 API 실패 - 존재하지 않는 회원")
    void deleteMyAccount_Fail_MemberNotFound() throws Exception {
        // given
        doThrow(new BusinessException(
            ErrorCode.MEMBER_NOT_FOUND,
            "memberId: " + testMemberId
        )).when(memberService).deleteMember(any());

        // when & then
        mockMvc.perform(delete("/api/members/me"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }
}
