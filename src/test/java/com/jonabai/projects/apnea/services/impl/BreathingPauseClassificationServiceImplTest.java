package com.jonabai.projects.apnea.services.impl;

import com.jonabai.projects.apnea.api.domain.BreathingPause;
import com.jonabai.projects.apnea.api.domain.BreathingPauseType;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class BreathingPauseClassificationServiceImplTest {
    private BreathingPauseClassificationServiceImpl breathingPauseClassificationService;

    @Before
    public void setUp() throws Exception {
        breathingPauseClassificationService = new BreathingPauseClassificationServiceImpl();
    }

    @Test
    public void classifyNullListNotThrowsException() throws Exception {

        breathingPauseClassificationService.classify(null);

    }

    @Test
    public void classifyNulListNotThrowsException() throws Exception {
        // given
        List<BreathingPause> pauses = new ArrayList<>();
        pauses.add(new BreathingPause("1", 1, 1f, 1f, BreathingPauseType.NOT_SET));
        pauses.add(new BreathingPause("2", 2, 2f, 2f, BreathingPauseType.NOT_SET));
        pauses.add(new BreathingPause("3", 3, 3f,
                3f + breathingPauseClassificationService.getApneaLimit(), BreathingPauseType.NOT_SET));

        // when
        breathingPauseClassificationService.classify(pauses);

        // then
        assertEquals(BreathingPauseType.NORMAL, pauses.get(0).getType());
        assertEquals(BreathingPauseType.NORMAL, pauses.get(1).getType());
        assertEquals(BreathingPauseType.APNEA, pauses.get(2).getType());
    }

}
