package com.jonabai.projects.apnea.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SilenceCheckerService Tests")
class SilenceCheckerServiceImplTest {

    private static final double SILENCE_THRESHOLD = 0.00001d;

    private SilenceCheckerServiceImpl silenceCheckerService;

    @BeforeEach
    void setUp() {
        silenceCheckerService = new SilenceCheckerServiceImpl(SILENCE_THRESHOLD);
    }

    @Test
    @DisplayName("Should return false for null array")
    void isSilenceForNullArrayReturnsFalse() {
        var isSilence = silenceCheckerService.isSilence(null);

        assertFalse(isSilence);
    }

    @Test
    @DisplayName("Should return false for empty array")
    void isSilenceForEmptyArrayReturnsFalse() {
        var isSilence = silenceCheckerService.isSilence(new double[]{});

        assertFalse(isSilence);
    }

    @Test
    @DisplayName("Should return false for loud audio")
    void isSilenceForLoudAudioReturnsFalse() {
        var buffer = new double[]{0.33, 0.34, 0.32, 0.31, 0.51};
        var isSilence = silenceCheckerService.isSilence(buffer);

        assertFalse(isSilence);
    }

    @Test
    @DisplayName("Should return true for silence")
    void isSilenceForSilentAudioReturnsTrue() {
        var buffer = new double[]{0d, 0d, 0d, 0d, 0.000008d};
        var isSilence = silenceCheckerService.isSilence(buffer);

        assertTrue(isSilence);
    }
}
