package knu.team1.be.boost.boostingScore.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import knu.team1.be.boost.boostingScore.entity.BoostingScore;
import lombok.Builder;

@Builder
public record BoostingScoreResponseDto(
    UUID memberId,
    Integer totalScore,
    Integer rank,
    LocalDateTime calculatedAt
) {

    public static BoostingScoreResponseDto from(BoostingScore boostingScore, Integer rank) {
        var member = boostingScore.getProjectMembership().getMember();
        return BoostingScoreResponseDto.builder()
            .memberId(member.getId())
            .totalScore(boostingScore.getTotalScore())
            .rank(rank)
            .calculatedAt(boostingScore.getCalculatedAt())
            .build();
    }
}

