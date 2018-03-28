package com.jonabai.projects.apnea.services.impl;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SilenceCheckerServiceImplTest {
    private static final double SILENCE_THRESHOLD = 0.00001d;

    private SilenceCheckerServiceImpl silenceCheckerService;

    @Before
    public void setUp() throws Exception {
        silenceCheckerService = new SilenceCheckerServiceImpl(SILENCE_THRESHOLD);
    }

    @Test
    public void isSilenceForNullArrayReturnsFalse() throws Exception {
        boolean isSilence = silenceCheckerService.isSilence(null);

        assertFalse(isSilence);
    }

    @Test
    public void isSilenceForEmptyArrayReturnsFalse() throws Exception {
        boolean isSilence = silenceCheckerService.isSilence(new double[] {});

        assertFalse(isSilence);
    }

    @Test
    public void isSilenceForArrayReturnsFalse() throws Exception {
        double[] buffer = new double[] {0.33, 0.34, 0.32, 0.31, 0.51};
        boolean isSilence = silenceCheckerService.isSilence(buffer);

        assertFalse(isSilence);
    }

    @Test
    public void isSilenceForArrayReturnsTrue() throws Exception {
        double[] buffer = new double[] {0d, 0d, 0d, 0d, 0.000008d};
        boolean isSilence = silenceCheckerService.isSilence(buffer);

        assertTrue(isSilence);
    }
}
