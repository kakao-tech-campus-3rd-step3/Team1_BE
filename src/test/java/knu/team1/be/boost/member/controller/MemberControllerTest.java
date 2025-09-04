package knu.team1.be.boost.member.controller;

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
import knu.team1.be.boost.common.exception.MemberNotFoundException;
import knu.team1.be.boost.member.dto.MemberResponseDto;
import knu.team1.be.boost.member.dto.MemberUpdateRequestDto;
import knu.team1.be.boost.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = MemberController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class
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
            "🤖",
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        given(memberService.getMember(any(UUID.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/members/me"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("테스트 유저"))
            .andExpect(jsonPath("$.profileEmoji").value("🤖"));
    }

    @Test
    @DisplayName("내 정보 조회 API 실패 - 존재하지 않는 회원")
    void getMyInfo_Fail_MemberNotFound() throws Exception {
        // given
        given(memberService.getMember(any(UUID.class)))
            .willThrow(new MemberNotFoundException(testMemberId));

        // when & then
        mockMvc.perform(get("/api/members/me"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("내 정보 수정 API 성공")
    void updateMyInfo_Success() throws Exception {
        // given
        MemberUpdateRequestDto requestDto = new MemberUpdateRequestDto("수정된 이름", "😎");
        MemberResponseDto responseDto = new MemberResponseDto(
            testMemberId,
            "수정된 이름",
            "😎",
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        given(memberService.updateMember(any(UUID.class),
            any(MemberUpdateRequestDto.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(patch("/api/members/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("수정된 이름"))
            .andExpect(jsonPath("$.profileEmoji").value("😎"));
    }

    @Test
    @DisplayName("내 정보 수정 API 실패 - 존재하지 않는 회원")
    void updateMyInfo_Fail_MemberNotFound() throws Exception {
        // given
        MemberUpdateRequestDto requestDto = new MemberUpdateRequestDto("수정된 이름", "😎");
        given(memberService.updateMember(any(UUID.class), any(MemberUpdateRequestDto.class)))
            .willThrow(new MemberNotFoundException(testMemberId));

        // when & then
        mockMvc.perform(patch("/api/members/me")
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
        doThrow(new MemberNotFoundException(testMemberId)).when(memberService)
            .deleteMember(any(UUID.class));

        // when & then
        mockMvc.perform(delete("/api/members/me"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }
}

