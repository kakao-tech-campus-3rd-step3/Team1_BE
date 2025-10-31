package knu.team1.be.boost.boostingScore.controller;

import java.util.List;
import java.util.UUID;
import knu.team1.be.boost.auth.dto.UserPrincipalDto;
import knu.team1.be.boost.boostingScore.dto.BoostingScoreResponseDto;
import knu.team1.be.boost.boostingScore.service.BoostingScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BoostingScoreController implements BoostingScoreApi {

    private final BoostingScoreService boostingScoreService;

    @Override
    public ResponseEntity<List<BoostingScoreResponseDto>> getBoostingScores(
        @PathVariable UUID projectId,
        @AuthenticationPrincipal UserPrincipalDto user
    ) {
        List<BoostingScoreResponseDto> response =
            boostingScoreService.getProjectBoostingScores(projectId, user.id());

        return ResponseEntity.ok(response);
    }
}

