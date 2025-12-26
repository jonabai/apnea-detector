package com.jonabai.projects.apnea.api.dto;

import com.jonabai.projects.apnea.api.domain.BreathingPause;
import com.jonabai.projects.apnea.api.domain.BreathingPauseType;

import java.util.List;

/**
 * Response DTO for file analysis results with severity breakdown.
 */
public record AnalysisResponse(
        String filename,
        int totalPauses,
        int normalCount,
        int hypopneaCount,
        int mildApneaCount,
        int moderateApneaCount,
        int severeApneaCount,
        int totalApneaCount,
        List<PauseDto> pauses
) {
    /**
     * Creates an AnalysisResponse from a list of BreathingPause objects.
     */
    public static AnalysisResponse from(String filename, List<BreathingPause> pauses) {
        var pauseDtos = pauses.stream()
                .map(PauseDto::from)
                .toList();

        var normalCount = (int) pauses.stream()
                .filter(p -> p.type() == BreathingPauseType.NORMAL)
                .count();

        var hypopneaCount = (int) pauses.stream()
                .filter(p -> p.type() == BreathingPauseType.HYPOPNEA)
                .count();

        var mildApneaCount = (int) pauses.stream()
                .filter(p -> p.type() == BreathingPauseType.MILD_APNEA)
                .count();

        var moderateApneaCount = (int) pauses.stream()
                .filter(p -> p.type() == BreathingPauseType.MODERATE_APNEA)
                .count();

        var severeApneaCount = (int) pauses.stream()
                .filter(p -> p.type() == BreathingPauseType.SEVERE_APNEA)
                .count();

        var totalApneaCount = (int) pauses.stream()
                .filter(p -> p.type().isHealthConcern())
                .count();

        return new AnalysisResponse(
                filename,
                pauses.size(),
                normalCount,
                hypopneaCount,
                mildApneaCount,
                moderateApneaCount,
                severeApneaCount,
                totalApneaCount,
                pauseDtos
        );
    }
}
