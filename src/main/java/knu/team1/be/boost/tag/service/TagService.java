package knu.team1.be.boost.tag.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final AccessPolicy accessPolicy;

    @Transactional
    public TagResponseDto createTag(
        UUID projectId,
        TagCreateRequestDto request,
        UserPrincipalDto user
    ) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PROJECT_NOT_FOUND, "projectId=" + projectId
            ));

        accessPolicy.ensureProjectMember(project.getId(), user.id());

        String trimmedName = request.name().trim();
        Optional<Tag> existingRecord = tagRepository.findByProjectIdAndNameIncludingDeleted(
            project.getId(), trimmedName);

        if (existingRecord.isEmpty()) {
            Tag newTag = Tag.create(project, trimmedName);
            tagRepository.save(newTag);
            return TagResponseDto.from(newTag);
        }

        Tag existingTag = existingRecord.get();

        if (existingTag.isDeleted()) {
            existingTag.reactivate();
            existingTag.update(trimmedName);
            tagRepository.save(existingTag);
            return TagResponseDto.from(existingTag);
        }

        throw new BusinessException(
            ErrorCode.DUPLICATED_TAG_NAME,
            "projectId=" + project.getId() + ", name=" + trimmedName
        );
    }

    @Transactional(readOnly = true)
    public List<TagResponseDto> getAllTags(
        UUID projectId,
        UserPrincipalDto user
    ) {
        if (!projectRepository.existsById(projectId)) {
            throw new BusinessException(
                ErrorCode.PROJECT_NOT_FOUND, "projectId=" + projectId
            );
        }

        accessPolicy.ensureProjectMember(projectId, user.id());

        List<Tag> tags = tagRepository.findAllByProjectId(projectId);

        return tags.stream()
            .map(TagResponseDto::from)
            .toList();
    }

    @Transactional
    public TagResponseDto updateTag(
        UUID projectId,
        UUID tagId,
        TagUpdateRequestDto request,
        UserPrincipalDto user
    ) {
        if (!projectRepository.existsById(projectId)) {
            throw new BusinessException(
                ErrorCode.PROJECT_NOT_FOUND, "projectId=" + projectId
            );
        }

        accessPolicy.ensureProjectMember(projectId, user.id());

        Tag tag = tagRepository.findById(tagId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.TAG_NOT_FOUND, "tagId=" + tagId
            ));

        tag.ensureTagInProject(projectId);

        String trimmedName = request.name().trim();
        Optional<Tag> existingRecord = tagRepository.findByProjectIdAndNameIncludingDeleted(
            projectId, trimmedName);

        // 동일한 이름의 다른 태그가 존재하는 경우
        if (existingRecord.isPresent()) {
            Tag existingTag = existingRecord.get();

            // 같은 태그면 그냥 이름만 변경
            if (existingTag.getId().equals(tagId)) {
                tag.update(trimmedName);
                return TagResponseDto.from(tag);
            }

            // 삭제된 태그라면 재활성화 + 현재 태그 삭제 (이름 덮어쓰기)
            if (existingTag.isDeleted()) {
                existingTag.reactivate();
                existingTag.update(trimmedName);
                tagRepository.delete(tag);
                return TagResponseDto.from(existingTag);
            }

            // 이미 활성화된 동일 이름 태그가 있다면 예외
            throw new BusinessException(
                ErrorCode.DUPLICATED_TAG_NAME,
                "projectId=" + projectId + ", name=" + trimmedName
            );
        }

        tag.update(trimmedName);

        return TagResponseDto.from(tag);
    }

    @Transactional
    public void deleteTag(
        UUID projectId,
        UUID tagId,
        UserPrincipalDto user
    ) {
        if (!projectRepository.existsById(projectId)) {
            throw new BusinessException(
                ErrorCode.PROJECT_NOT_FOUND, "projectId=" + projectId
            );
        }

        accessPolicy.ensureProjectMember(projectId, user.id());

        Tag tag = tagRepository.findById(tagId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.TAG_NOT_FOUND, "tagId=" + tagId
            ));

        tag.ensureTagInProject(projectId);

        taskRepository.detachTagFromAllTasks(tag.getId());

        tagRepository.delete(tag);
    }
}
