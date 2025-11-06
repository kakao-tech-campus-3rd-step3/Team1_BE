package knu.team1.be.boost.tag.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.security.filter.JwtAuthFilter;
import knu.team1.be.boost.tag.dto.TagCreateRequestDto;
import knu.team1.be.boost.tag.dto.TagResponseDto;
import knu.team1.be.boost.tag.dto.TagUpdateRequestDto;
import knu.team1.be.boost.tag.service.TagService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
    controllers = TagController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthFilter.class
    )
)
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TagService tagService;

    private static final UUID PROJECT_ID = UUID.fromString("a1b2c3d4-e5f6-7890-1234-567890abcdef");
    private static final UUID TAG_ID = UUID.fromString("770e8400-e29b-41d4-a716-446655440000");

    @Nested
    @DisplayName("태그 생성")
    class CreateTag {

        @Test
        @DisplayName("태그 생성 성공 - 201 Created + Location 헤더")
        void createTag_success() throws Exception {
            // given
            TagCreateRequestDto request = new TagCreateRequestDto("피드백");
            TagResponseDto response = new TagResponseDto(TAG_ID, "피드백");

            given(tagService.createTag(eq(PROJECT_ID), any(TagCreateRequestDto.class),
                any(UserPrincipalDto.class)))
                .willReturn(response);

            // when & then
            mockMvc.perform(
                    post("/api/projects/{projectId}/tags", PROJECT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(
                    header().string("Location", "/api/projects/" + PROJECT_ID + "/tags/" + TAG_ID))
                .andExpect(jsonPath("$.tagId").value(TAG_ID.toString()))
                .andExpect(jsonPath("$.name").value("피드백"));
        }

        @Test
        @DisplayName("태그 생성 실패 - 400 (빈 이름)")
        void createTag_fail_blankName() throws Exception {
            // given
            TagCreateRequestDto request = new TagCreateRequestDto("");

            // when & then
            mockMvc.perform(
                    post("/api/projects/{projectId}/tags", PROJECT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("태그 목록 조회")
    class GetAllTags {

        @Test
        @DisplayName("태그 목록 조회 성공 - 200 OK")
        void getAllTags_success() throws Exception {
            // given
            List<TagResponseDto> responses = List.of(
                new TagResponseDto(TAG_ID, "피드백"),
                new TagResponseDto(UUID.fromString("770e8400-e29b-41d4-a716-446655440111"), "멘토링")
            );

            given(tagService.getAllTags(eq(PROJECT_ID), any(UserPrincipalDto.class)))
                .willReturn(responses);

            // when & then
            mockMvc.perform(get("/api/projects/{projectId}/tags", PROJECT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tagId").value(TAG_ID.toString()))
                .andExpect(jsonPath("$[0].name").value("피드백"))
                .andExpect(jsonPath("$[1].name").value("멘토링"));
        }
    }

    @Nested
    @DisplayName("태그 수정")
    class UpdateTag {

        @Test
        @DisplayName("태그 수정 성공 - 200 OK")
        void updateTag_success() throws Exception {
            // given
            TagUpdateRequestDto request = new TagUpdateRequestDto("수정된 태그명");
            TagResponseDto response = new TagResponseDto(TAG_ID, "수정된 태그명");

            given(tagService.updateTag(eq(PROJECT_ID), eq(TAG_ID), any(TagUpdateRequestDto.class),
                any(UserPrincipalDto.class)))
                .willReturn(response);

            // when & then
            mockMvc.perform(
                    patch("/api/projects/{projectId}/tags/{tagId}", PROJECT_ID, TAG_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tagId").value(TAG_ID.toString()))
                .andExpect(jsonPath("$.name").value("수정된 태그명"));
        }

        @Test
        @DisplayName("태그 수정 실패 - 400 (빈 이름)")
        void updateTag_fail_blankName() throws Exception {
            // given
            TagUpdateRequestDto request = new TagUpdateRequestDto("");

            // when & then
            mockMvc.perform(
                    patch("/api/projects/{projectId}/tags/{tagId}", PROJECT_ID, TAG_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("태그 삭제")
    class DeleteTag {

        @Test
        @DisplayName("태그 삭제 성공 - 204 No Content")
        void deleteTag_success() throws Exception {
            // when & then
            mockMvc.perform(
                    delete("/api/projects/{projectId}/tags/{tagId}", PROJECT_ID, TAG_ID)
                )
                .andExpect(status().isNoContent());
        }
    }
}
