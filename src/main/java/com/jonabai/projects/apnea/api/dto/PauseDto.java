package com.jonabai.projects.apnea.api.dto;

import com.jonabai.projects.apnea.api.domain.BreathingPause;
import com.jonabai.projects.apnea.api.domain.BreathingPauseType;

/**
 * DTO for a breathing pause in API responses.
 */
public record PauseDto(
        int index,
        float start,
        float end,
        float duration,
        BreathingPauseType type
) {
    /**
     * Creates a PauseDto from a BreathingPause domain object.
     */
    public static PauseDto from(BreathingPause pause) {
        return new PauseDto(
                pause.index(),
                pause.start(),
                pause.end(),
                pause.duration(),
                pause.type()
        );
    }
}
