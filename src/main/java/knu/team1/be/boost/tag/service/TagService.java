package knu.team1.be.boost.tag.service;

import jakarta.transaction.Transactional;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.common.policy.AccessPolicy;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.project.repository.ProjectRepository;
import knu.team1.be.boost.tag.dto.TagCreateRequestDto;
import knu.team1.be.boost.tag.dto.TagResponseDto;
import knu.team1.be.boost.tag.entity.Tag;
import knu.team1.be.boost.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
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

        tagRepository.findByProjectIdAndName(project.getId(), request.name()).ifPresent(t -> {
            throw new BusinessException(ErrorCode.DUPLICATED_TAG_NAME,
                "projectId=" + project.getId() + ", name=" + request.name());
        });

        Tag tag = Tag.create(project, request.name());

        Tag saved = tagRepository.save(tag);

        return TagResponseDto.from(saved);
    }
}
