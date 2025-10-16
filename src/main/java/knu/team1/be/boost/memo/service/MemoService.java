package knu.team1.be.boost.memo.service;

import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.common.exception.BusinessException;
import knu.team1.be.boost.common.exception.ErrorCode;
import knu.team1.be.boost.common.policy.AccessPolicy;
import knu.team1.be.boost.member.repository.MemberRepository;
import knu.team1.be.boost.memo.dto.MemoCreateRequestDto;
import knu.team1.be.boost.memo.dto.MemoItemResponseDto;
import knu.team1.be.boost.memo.dto.MemoResponseDto;
import knu.team1.be.boost.memo.dto.MemoUpdateRequestDto;
import knu.team1.be.boost.memo.entity.Memo;
import knu.team1.be.boost.memo.repository.MemoRepository;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemoService {

    private final MemoRepository memoRepository;
    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;
    private final AccessPolicy accessPolicy;

    @Transactional
    public MemoResponseDto createMemo(
        UUID projectId,
        UUID memberId,
        MemoCreateRequestDto requestDto
    ) {
        Project project = findProjectAndVerifyMember(projectId, memberId);

        Memo memo = Memo.builder()
            .project(project)
            .title(requestDto.title())
            .content(requestDto.content())
            .build();

        Memo savedMemo = memoRepository.save(memo);

        return MemoResponseDto.from(savedMemo);
    }

    public List<MemoItemResponseDto> findMemosByProjectId(UUID projectId, UUID memberId) {
        accessPolicy.ensureProjectMember(projectId, memberId);

        List<Memo> memos = memoRepository.findAllByProjectId(projectId);

        return memos.stream()
            .map(MemoItemResponseDto::from)
            .toList();
    }

    public MemoResponseDto findMemoById(
        UUID projectId,
        UUID memoId,
        UUID memberId
    ) {
        Memo memo = findMemoAndVerifyAccess(projectId, memoId, memberId);

        return MemoResponseDto.from(memo);
    }

    @Transactional
    public MemoResponseDto updateMemo(
        UUID projectId,
        UUID memoId,
        UUID memberId,
        MemoUpdateRequestDto requestDto
    ) {
        Memo memo = findMemoAndVerifyAccess(projectId, memoId, memberId);

        memo.update(requestDto.title(), requestDto.content());

        return MemoResponseDto.from(memo);
    }

    @Transactional
    public void deleteMemo(
        UUID projectId,
        UUID memoId,
        UUID memberId
    ) {
        Memo memo = findMemoAndVerifyAccess(projectId, memoId, memberId);

        memoRepository.delete(memo);
    }

    private Project findProjectAndVerifyMember(UUID projectId, UUID memberId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PROJECT_NOT_FOUND, "projectId: " + projectId
            ));
        
        accessPolicy.ensureProjectMember(projectId, memberId);

        return project;
    }

    private Memo findMemoAndVerifyAccess(UUID projectId, UUID memoId, UUID memberId) {
        accessPolicy.ensureProjectMember(projectId, memberId);

        Memo memo = memoRepository.findById(memoId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.MEMO_NOT_FOUND, "memoId: " + memoId
            ));

        memo.ensureMemoInProject(projectId);

        return memo;
    }
}

