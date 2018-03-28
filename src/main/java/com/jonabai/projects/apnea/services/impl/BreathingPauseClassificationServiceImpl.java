package com.jonabai.projects.apnea.services.impl;

import com.jonabai.projects.apnea.api.domain.BreathingPause;
import com.jonabai.projects.apnea.api.domain.BreathingPauseType;
import com.jonabai.projects.apnea.services.BreathingPauseClassificationService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * BreathingPauseClassificationService implementation
 */
@Service
public class BreathingPauseClassificationServiceImpl implements BreathingPauseClassificationService {

    private static final float APNEA_LIMIT = 4.5f;

    @Override
    public void classify(List<BreathingPause> pauses) {
        if(pauses == null)
            return;
        pauses.stream().parallel().forEach(this::classify);
    }

    public float getApneaLimit() {
        return APNEA_LIMIT;
    }

    private void classify(BreathingPause pause) {
        if(pause.getEnd() - pause.getStart() >= APNEA_LIMIT) {
            pause.setType(BreathingPauseType.APNEA);
        } else {
            pause.setType(BreathingPauseType.NORMAL);
        }
    }
}
