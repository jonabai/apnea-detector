package com.jonabai.projects.apnea.services.impl;

import com.jonabai.projects.apnea.api.domain.BreathingPause;
import com.jonabai.projects.apnea.api.domain.BreathingPauseType;
import com.jonabai.projects.apnea.services.BreathingPauseClassificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service implementation for classifying breathing pauses based on duration
 * with severity gradation support.
 */
@Service
public class BreathingPauseClassificationServiceImpl implements BreathingPauseClassificationService {

    private final float hypopneaThresholdSeconds;
    private final float mildApneaThresholdSeconds;
    private final float moderateApneaThresholdSeconds;
    private final float severeApneaThresholdSeconds;

    public BreathingPauseClassificationServiceImpl(
            @Value("${apnea.classification.hypopnea.threshold:3.0}") float hypopneaThresholdSeconds,
            @Value("${apnea.classification.mild.threshold:10.0}") float mildApneaThresholdSeconds,
            @Value("${apnea.classification.moderate.threshold:20.0}") float moderateApneaThresholdSeconds,
            @Value("${apnea.classification.severe.threshold:30.0}") float severeApneaThresholdSeconds) {
        this.hypopneaThresholdSeconds = hypopneaThresholdSeconds;
        this.mildApneaThresholdSeconds = mildApneaThresholdSeconds;
        this.moderateApneaThresholdSeconds = moderateApneaThresholdSeconds;
        this.severeApneaThresholdSeconds = severeApneaThresholdSeconds;
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

    public float getHypopneaThresholdSeconds() {
        return hypopneaThresholdSeconds;
    }

    public float getMildApneaThresholdSeconds() {
        return mildApneaThresholdSeconds;
    }

    public float getModerateApneaThresholdSeconds() {
        return moderateApneaThresholdSeconds;
    }

    public float getSevereApneaThresholdSeconds() {
        return severeApneaThresholdSeconds;
    }

    private BreathingPause classify(BreathingPause pause) {
        var duration = pause.duration();
        BreathingPauseType type;

        if (duration >= severeApneaThresholdSeconds) {
            type = BreathingPauseType.SEVERE_APNEA;
        } else if (duration >= moderateApneaThresholdSeconds) {
            type = BreathingPauseType.MODERATE_APNEA;
        } else if (duration >= mildApneaThresholdSeconds) {
            type = BreathingPauseType.MILD_APNEA;
        } else if (duration >= hypopneaThresholdSeconds) {
            type = BreathingPauseType.HYPOPNEA;
        } else {
            type = BreathingPauseType.NORMAL;
        }

        return pause.withType(type);
    }
}
