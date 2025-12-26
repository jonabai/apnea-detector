package com.jonabai.projects.apnea.services;

import com.jonabai.projects.apnea.api.domain.BreathingPause;

import java.util.List;

/**
 * Service for classifying breathing pauses as normal or apnea.
 */
public interface BreathingPauseClassificationService {

    /**
     * Classifies each breathing pause and returns a new list with classified pauses.
     *
     * @param pauses list of breathing pause elements
     * @return new list with classified breathing pauses
     */
    List<BreathingPause> classify(List<BreathingPause> pauses);
}
