package com.jonabai.projects.apnea.services;

import com.jonabai.projects.apnea.api.domain.BreathingPause;

import java.util.List;

/**
 * A Breathing pause classification service
 */
public interface BreathingPauseClassificationService {

    /**
     * Assigns the correct type property for each breathing pause element in the list
     * @param pauses list of breathing pause elements
     */
    void classify(List<BreathingPause> pauses);
}
