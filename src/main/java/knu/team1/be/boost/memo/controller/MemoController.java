package knu.team1.be.boost.memo.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.memo.dto.MemoCreateRequestDto;
import knu.team1.be.boost.memo.dto.MemoItemResponseDto;
import knu.team1.be.boost.memo.dto.MemoResponseDto;
import knu.team1.be.boost.memo.dto.MemoUpdateRequestDto;
import knu.team1.be.boost.memo.service.MemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemoController implements MemoApi {

    private final MemoService memoService;

    @Override
    public ResponseEntity<MemoResponseDto> createMemo(
        @PathVariable UUID projectId,
        @AuthenticationPrincipal UserPrincipalDto user,
        @Valid @RequestBody MemoCreateRequestDto requestDto
    ) {
        MemoResponseDto responseDto = memoService.createMemo(
            projectId,
            user.id(),
            requestDto
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Override
    public ResponseEntity<List<MemoItemResponseDto>> getMemoList(
        @PathVariable UUID projectId,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        List<MemoItemResponseDto> response = memoService.findMemosByProjectId(projectId, user.id());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<MemoResponseDto> getMemo(
        @PathVariable UUID projectId,
        @PathVariable UUID memoId,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        MemoResponseDto responseDto = memoService.findMemoById(
            projectId,
            memoId,
            user.id()
        );
        return ResponseEntity.ok(responseDto);
    }

    @Override
    public ResponseEntity<MemoResponseDto> updateMemo(
        @PathVariable UUID projectId,
        @PathVariable UUID memoId,
        @AuthenticationPrincipal UserPrincipalDto user,
        @Valid @RequestBody MemoUpdateRequestDto requestDto
    ) {
        MemoResponseDto responseDto = memoService.updateMemo(
            projectId,
            memoId,
            user.id(),
            requestDto
        );
        return ResponseEntity.ok(responseDto);
    }

    @Override
    public ResponseEntity<Void> deleteMemo(
        @PathVariable UUID projectId,
        @PathVariable UUID memoId,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        memoService.deleteMemo(
            projectId,
            memoId,
            user.id()
        );
        return ResponseEntity.noContent().build();
    }
}
