package knu.team1.be.boost.tag.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.common.policy.AccessPolicy;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.project.repository.ProjectRepository;
import knu.team1.be.boost.tag.dto.TagCreateRequestDto;
import knu.team1.be.boost.tag.dto.TagResponseDto;
import knu.team1.be.boost.tag.dto.TagUpdateRequestDto;
import knu.team1.be.boost.tag.entity.Tag;
import knu.team1.be.boost.tag.repository.TagRepository;
import knu.team1.be.boost.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    TagRepository tagRepository;
    @Mock
    TaskRepository taskRepository;
    @Mock
    ProjectRepository projectRepository;
    @Mock
    AccessPolicy accessPolicy;

    TagService tagService;

    UUID userId;
    UserPrincipalDto user;
    UUID projectId;
    Project project;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = UserPrincipalDto.from(userId, "tester", "avatar");
        projectId = UUID.randomUUID();
        project = Fixtures.project(projectId);

        tagService = new TagService(
            tagRepository,
            taskRepository,
            projectRepository,
            accessPolicy
        );
    }

    @Nested
    @DisplayName("태그 생성")
    class CreateTag {

        @Test
        @DisplayName("태그 생성 성공 - 신규 이름")
        void success_newName() {
            // given
            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
            doNothing().when(accessPolicy).ensureProjectMember(eq(projectId), eq(userId));

            String rawName = "  피드백  ";
            String trimmed = "피드백";
            given(tagRepository.findByProjectIdAndNameIncludingDeleted(projectId, trimmed))
                .willReturn(Optional.empty());

            given(tagRepository.save(any(Tag.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            TagCreateRequestDto request = Fixtures.reqCreate(rawName);

            // when
            TagResponseDto res = tagService.createTag(projectId, request, user);

            // then
            assertThat(res.name()).isEqualTo(trimmed);
            ArgumentCaptor<Tag> cap = ArgumentCaptor.forClass(Tag.class);
            verify(tagRepository).save(cap.capture());
            assertThat(cap.getValue().getProject()).isEqualTo(project);
            assertThat(cap.getValue().getName()).isEqualTo(trimmed);
            verify(tagRepository).findByProjectIdAndNameIncludingDeleted(projectId, trimmed);
        }

        @Test
        @DisplayName("태그 생성 성공 - 삭제된 동일 이름 태그 재활성화")
        void success_reactivateDeleted() {
            // given
            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
            doNothing().when(accessPolicy).ensureProjectMember(eq(projectId), eq(userId));

            String name = "피드백";
            Tag deletedExisting = mock(Tag.class);
            UUID existingId = UUID.randomUUID();

            given(deletedExisting.getId()).willReturn(existingId);
            given(deletedExisting.getName()).willReturn(name);
            given(deletedExisting.isDeleted()).willReturn(true);
            given(tagRepository.findByProjectIdAndNameIncludingDeleted(projectId, name))
                .willReturn(Optional.of(deletedExisting));

            given(tagRepository.save(any(Tag.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            TagCreateRequestDto request = Fixtures.reqCreate(name);

            // when
            TagResponseDto res = tagService.createTag(projectId, request, user);

            // then
            assertThat(res.tagId()).isEqualTo(existingId);
            assertThat(res.name()).isEqualTo(name);
            verify(deletedExisting).reactivate();
            verify(deletedExisting).update(name);
            verify(tagRepository).save(deletedExisting);
        }

        @Test
        @DisplayName("태그 생성 실패 - 404 (프로젝트 없음)")
        void fail_projectNotFound() {
            // given
            given(projectRepository.findById(projectId)).willReturn(Optional.empty());
            TagCreateRequestDto request = Fixtures.reqCreate("피드백");

            // when & then
            assertThatThrownBy(() -> tagService.createTag(projectId, request, user))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);

            verifyNoInteractions(accessPolicy, tagRepository);
        }

        @Test
        @DisplayName("태그 생성 실패 - 409 (중복 이름 존재)")
        void fail_duplicateNameActive() {
            // given
            given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
            doNothing().when(accessPolicy).ensureProjectMember(eq(projectId), eq(userId));

            String name = "피드백";
            Tag existing = mock(Tag.class);
            given(existing.isDeleted()).willReturn(false);
            given(tagRepository.findByProjectIdAndNameIncludingDeleted(projectId, name))
                .willReturn(Optional.of(existing));

            TagCreateRequestDto request = Fixtures.reqCreate(name);

            // when & then
            assertThatThrownBy(() -> tagService.createTag(projectId, request, user))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATED_TAG_NAME);

            verify(tagRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("태그 전체 조회")
    class GetAllTags {

        @Test
        @DisplayName("태그 전체 조회 성공")
        void success_getAll() {
            // given
            given(projectRepository.existsById(projectId)).willReturn(true);
            doNothing().when(accessPolicy).ensureProjectMember(eq(projectId), eq(userId));

            Tag t1 = Fixtures.tag(UUID.randomUUID(), "피드백", project);
            Tag t2 = Fixtures.tag(UUID.randomUUID(), "멘토링", project);

            given(tagRepository.findAllByProjectId(projectId)).willReturn(List.of(t1, t2));

            // when
            List<TagResponseDto> res = tagService.getAllTags(projectId, user);

            // then
            assertThat(res).hasSize(2)
                .extracting(TagResponseDto::name)
                .containsExactlyInAnyOrder("피드백", "멘토링");
        }

        @Test
        @DisplayName("태그 전체 조회 실패 - 404 (프로젝트 없음)")
        void fail_projectNotFound() {
            // given
            given(projectRepository.existsById(projectId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> tagService.getAllTags(projectId, user))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);

            verifyNoInteractions(accessPolicy, tagRepository);
        }
    }

    @Nested
    @DisplayName("태그 수정")
    class UpdateTag {

        @Test
        @DisplayName("태그 수정 성공 - 고유한 새 이름으로 변경")
        void success_updateUniqueName() {
            // given
            UUID tagId = UUID.randomUUID();
            given(projectRepository.existsById(projectId)).willReturn(true);
            doNothing().when(accessPolicy).ensureProjectMember(eq(projectId), eq(userId));

            Tag existing = Fixtures.tag(tagId, "기존", project);
            given(tagRepository.findById(tagId)).willReturn(Optional.of(existing));

            String newName = "  새이름  ";
            String trimmed = "새이름";
            given(tagRepository.findByProjectIdAndNameIncludingDeleted(projectId, trimmed))
                .willReturn(Optional.empty());

            TagUpdateRequestDto request = Fixtures.reqUpdate(newName);

            // when
            TagResponseDto res = tagService.updateTag(projectId, tagId, request, user);

            // then
            assertThat(res.tagId()).isEqualTo(tagId);
            assertThat(res.name()).isEqualTo(trimmed);
            assertThat(existing.getName()).isEqualTo(trimmed);
            verify(tagRepository, never()).delete(any());
            verify(taskRepository, never()).transferTagToAnotherTag(any(), any());
        }

        @Test
        @DisplayName("태그 수정 성공 - 동일 태그로 동일 이름 충돌 (자기 자신) → 단순 업데이트")
        void success_sameRecordJustUpdate() {
            // given
            UUID tagId = UUID.randomUUID();
            given(projectRepository.existsById(projectId)).willReturn(true);
            doNothing().when(accessPolicy).ensureProjectMember(eq(projectId), eq(userId));

            Tag current = Fixtures.tag(tagId, "기존", project);
            given(tagRepository.findById(tagId)).willReturn(Optional.of(current));

            String targetName = "피드백 ";
            String trimmed = "피드백";

            Tag same = mock(Tag.class);
            given(same.getId()).willReturn(tagId);
            given(tagRepository.findByProjectIdAndNameIncludingDeleted(projectId, trimmed))
                .willReturn(Optional.of(same));

            TagUpdateRequestDto request = Fixtures.reqUpdate(targetName);

            // when
            TagResponseDto res = tagService.updateTag(projectId, tagId, request, user);

            // then
            assertThat(res.tagId()).isEqualTo(tagId);
            assertThat(res.name()).isEqualTo(trimmed);
            assertThat(current.getName()).isEqualTo(trimmed);
            verify(tagRepository, never()).delete(any());
            verify(taskRepository, never()).transferTagToAnotherTag(any(), any());
        }

        @Test
        @DisplayName("태그 수정 성공 - 삭제된 동일 이름 태그 존재 → 기존(삭제된) 재활성화 + 태스크 태그 이관 + 현재 태그 삭제")
        void success_mergeWithDeletedDuplicate() {
            // given
            UUID currentId = UUID.randomUUID();
            UUID deletedId = UUID.randomUUID();
            given(projectRepository.existsById(projectId)).willReturn(true);
            doNothing().when(accessPolicy).ensureProjectMember(eq(projectId), eq(userId));

            Tag current = Fixtures.tag(currentId, "기존", project);
            given(tagRepository.findById(currentId)).willReturn(Optional.of(current));

            String targetName = "피드백";
            Tag deletedDup = mock(Tag.class);
            given(deletedDup.getId()).willReturn(deletedId);
            given(deletedDup.getName()).willReturn(targetName);
            given(deletedDup.isDeleted()).willReturn(true);

            given(tagRepository.findByProjectIdAndNameIncludingDeleted(projectId, targetName))
                .willReturn(Optional.of(deletedDup));

            TagUpdateRequestDto request = Fixtures.reqUpdate(targetName);

            // when
            TagResponseDto res = tagService.updateTag(projectId, currentId, request, user);

            // then
            assertThat(res.tagId()).isEqualTo(deletedId);
            assertThat(res.name()).isEqualTo(targetName);
            verify(deletedDup).reactivate();
            verify(deletedDup).update(targetName);
            verify(taskRepository).transferTagToAnotherTag(currentId, deletedId);
            verify(tagRepository).delete(current);
        }

        @Test
        @DisplayName("태그 수정 실패 - 404 (프로젝트 없음)")
        void fail_projectNotFound() {
            // given
            UUID tagId = UUID.randomUUID();
            given(projectRepository.existsById(projectId)).willReturn(false);

            TagUpdateRequestDto request = Fixtures.reqUpdate("변경");

            // when & then
            assertThatThrownBy(() -> tagService.updateTag(projectId, tagId, request, user))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);

            verifyNoInteractions(accessPolicy, tagRepository, taskRepository);
        }

        @Test
        @DisplayName("태그 수정 실패 - 404 (태그 없음)")
        void fail_tagNotFound() {
            // given
            UUID tagId = UUID.randomUUID();
            given(projectRepository.existsById(projectId)).willReturn(true);
            doNothing().when(accessPolicy).ensureProjectMember(eq(projectId), eq(userId));
            given(tagRepository.findById(tagId)).willReturn(Optional.empty());

            TagUpdateRequestDto request = Fixtures.reqUpdate("변경");

            // when & then
            assertThatThrownBy(() -> tagService.updateTag(projectId, tagId, request, user))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TAG_NOT_FOUND);

            verifyNoInteractions(taskRepository);
        }

        @Test
        @DisplayName("태그 수정 실패 - 409 (다른 프로젝트의 태그)")
        void fail_tagNotInProject() {
            // given
            UUID tagId = UUID.randomUUID();
            UUID otherProjectId = UUID.randomUUID();
            Project other = Fixtures.project(otherProjectId);

            given(projectRepository.existsById(projectId)).willReturn(true);
            doNothing().when(accessPolicy).ensureProjectMember(eq(projectId), eq(userId));

            Tag tag = Fixtures.tag(tagId, "기존", other);
            given(tagRepository.findById(tagId)).willReturn(Optional.of(tag));

            TagUpdateRequestDto request = Fixtures.reqUpdate("변경");

            // when & then
            assertThatThrownBy(() -> tagService.updateTag(projectId, tagId, request, user))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TAG_NOT_IN_PROJECT);

            verifyNoInteractions(taskRepository);
        }

        @Test
        @DisplayName("태그 수정 실패 - 409 (활성 중복 이름 존재)")
        void fail_duplicateActiveName() {
            // given
            UUID tagId = UUID.randomUUID();
            given(projectRepository.existsById(projectId)).willReturn(true);
            doNothing().when(accessPolicy).ensureProjectMember(eq(projectId), eq(userId));

            Tag current = Fixtures.tag(tagId, "기존", project);
            given(tagRepository.findById(tagId)).willReturn(Optional.of(current));

            String targetName = "피드백";
            Tag activeDup = mock(Tag.class);
            given(activeDup.getId()).willReturn(UUID.randomUUID());
            given(activeDup.isDeleted()).willReturn(false);

            given(tagRepository.findByProjectIdAndNameIncludingDeleted(projectId, targetName))
                .willReturn(Optional.of(activeDup));

            TagUpdateRequestDto request = Fixtures.reqUpdate(targetName);

            // when & then
            assertThatThrownBy(() -> tagService.updateTag(projectId, tagId, request, user))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATED_TAG_NAME);

            verify(taskRepository, never()).transferTagToAnotherTag(any(), any());
            verify(tagRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("태그 삭제")
    class DeleteTag {

        @Test
        @DisplayName("태그 삭제 성공")
        void success_delete() {
            // given
            UUID tagId = UUID.randomUUID();
            given(projectRepository.existsById(projectId)).willReturn(true);
            doNothing().when(accessPolicy).ensureProjectMember(eq(projectId), eq(userId));

            Tag tag = Fixtures.tag(tagId, "삭제대상", project);
            given(tagRepository.findById(tagId)).willReturn(Optional.of(tag));

            // when
            tagService.deleteTag(projectId, tagId, user);

            // then
            verify(taskRepository).detachTagFromAllTasks(tagId);
            verify(tagRepository).delete(tag);
        }

        @Test
        @DisplayName("태그 삭제 실패 - 404 (프로젝트 없음)")
        void fail_projectNotFound() {
            // given
            UUID tagId = UUID.randomUUID();
            given(projectRepository.existsById(projectId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> tagService.deleteTag(projectId, tagId, user))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);

            verifyNoInteractions(accessPolicy, tagRepository, taskRepository);
        }

        @Test
        @DisplayName("태그 삭제 실패 - 404 (태그 없음)")
        void fail_tagNotFound() {
            // given
            UUID tagId = UUID.randomUUID();
            given(projectRepository.existsById(projectId)).willReturn(true);
            doNothing().when(accessPolicy).ensureProjectMember(eq(projectId), eq(userId));
            given(tagRepository.findById(tagId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> tagService.deleteTag(projectId, tagId, user))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TAG_NOT_FOUND);

            verify(taskRepository, never()).detachTagFromAllTasks(any());
            verify(tagRepository, never()).delete(any());
        }

        @Test
        @DisplayName("태그 삭제 실패 - 409 (다른 프로젝트의 태그)")
        void fail_tagNotInProject() {
            // given
            UUID tagId = UUID.randomUUID();
            UUID otherProjectId = UUID.randomUUID();
            Project other = Fixtures.project(otherProjectId);

            given(projectRepository.existsById(projectId)).willReturn(true);
            doNothing().when(accessPolicy).ensureProjectMember(eq(projectId), eq(userId));

            Tag tag = Fixtures.tag(tagId, "삭제불가", other);
            given(tagRepository.findById(tagId)).willReturn(Optional.of(tag));

            // when & then
            assertThatThrownBy(() -> tagService.deleteTag(projectId, tagId, user))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TAG_NOT_IN_PROJECT);

            verify(taskRepository, never()).detachTagFromAllTasks(any());
            verify(tagRepository, never()).delete(any());
        }
    }

    static class Fixtures {

        static Project project(UUID id) {
            return Project.builder()
                .id(id)
                .name("테스트 프로젝트")
                .defaultReviewerCount(1)
                .build();
        }

        static Tag tag(UUID id, String name, Project project) {
            return Tag.builder()
                .id(id)
                .project(project)
                .name(name)
                .build();
        }

        static TagCreateRequestDto reqCreate(String name) {
            return new TagCreateRequestDto(name);
        }

        static TagUpdateRequestDto reqUpdate(String name) {
            return new TagUpdateRequestDto(name);
        }
    }
}
