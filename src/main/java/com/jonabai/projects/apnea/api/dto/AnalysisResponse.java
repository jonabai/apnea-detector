package com.jonabai.projects.apnea.api.dto;

import com.jonabai.projects.apnea.api.domain.BreathingPause;
import com.jonabai.projects.apnea.api.domain.BreathingPauseType;

import java.util.List;

/**
 * Response DTO for file analysis results.
 */
public record AnalysisResponse(
        String filename,
        int totalPauses,
        int apneaCount,
        int normalCount,
        List<PauseDto> pauses
) {
    /**
     * Creates an AnalysisResponse from a list of BreathingPause objects.
     */
    public static AnalysisResponse from(String filename, List<BreathingPause> pauses) {
        var pauseDtos = pauses.stream()
                .map(PauseDto::from)
                .toList();

        var apneaCount = (int) pauses.stream()
                .filter(p -> p.type() == BreathingPauseType.APNEA)
                .count();

        var normalCount = (int) pauses.stream()
                .filter(p -> p.type() == BreathingPauseType.NORMAL)
                .count();

        return new AnalysisResponse(
                filename,
                pauses.size(),
                apneaCount,
                normalCount,
                pauseDtos
        );
    }
}
