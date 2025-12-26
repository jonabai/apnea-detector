package com.jonabai.projects.apnea.services.impl;

import com.jonabai.projects.apnea.api.domain.BreathingPause;
import com.jonabai.projects.apnea.api.domain.BreathingPauseType;
import com.jonabai.projects.apnea.services.BreathingPauseClassificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service implementation for classifying breathing pauses based on duration.
 */
@Service
public class BreathingPauseClassificationServiceImpl implements BreathingPauseClassificationService {

    private final float apneaThresholdSeconds;

    public BreathingPauseClassificationServiceImpl(
            @Value("${apnea.classification.threshold:4.5}") float apneaThresholdSeconds) {
        this.apneaThresholdSeconds = apneaThresholdSeconds;
    }

    @Override
    public List<BreathingPause> classify(List<BreathingPause> pauses) {
        if (pauses == null || pauses.isEmpty()) {
            return List.of();
        }

        return pauses.parallelStream()
                .map(this::classify)
                .toList();
    }

    public float getApneaThresholdSeconds() {
        return apneaThresholdSeconds;
    }

    private BreathingPause classify(BreathingPause pause) {
        var type = pause.duration() >= apneaThresholdSeconds
                ? BreathingPauseType.APNEA
                : BreathingPauseType.NORMAL;
        return pause.withType(type);
    }
}
