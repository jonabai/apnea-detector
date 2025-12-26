package com.jonabai.projects.apnea.services.impl;

import com.jonabai.projects.apnea.api.domain.BreathingPause;
import com.jonabai.projects.apnea.api.domain.BreathingPauseType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BreathingPauseClassificationService Tests")
class BreathingPauseClassificationServiceImplTest {

    private BreathingPauseClassificationServiceImpl classificationService;

    @BeforeEach
    void setUp() {
        classificationService = new BreathingPauseClassificationServiceImpl(4.5f);
    }

    @Test
    @DisplayName("Should return empty list for null input")
    void classifyNullListReturnsEmpty() {
        var result = classificationService.classify(null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list for empty input")
    void classifyEmptyListReturnsEmpty() {
        var result = classificationService.classify(List.of());
        assertTrue(result.isEmpty());
    }

    @ParameterizedTest
    @DisplayName("Should classify pauses correctly based on duration")
    @CsvSource({
            "1.0, 2.0, NORMAL",
            "0.0, 4.49, NORMAL",
            "0.0, 4.5, APNEA",
            "0.0, 10.0, APNEA"
    })
    void classifyBasedOnDuration(float start, float end, BreathingPauseType expectedType) {
        var pause = BreathingPause.unclassified("test.wav", 1, start, end);

        var result = classificationService.classify(List.of(pause));

        assertEquals(1, result.size());
        assertEquals(expectedType, result.get(0).type());
    }

    @Test
    @DisplayName("Should classify multiple pauses correctly")
    void classifyMultiplePauses() {
        var pauses = List.of(
                BreathingPause.unclassified("1", 1, 1f, 2f),
                BreathingPause.unclassified("2", 2, 2f, 3f),
                BreathingPause.unclassified("3", 3, 3f, 3f + classificationService.getApneaThresholdSeconds())
        );

        var result = classificationService.classify(pauses);

        assertAll(
                () -> assertEquals(BreathingPauseType.NORMAL, result.get(0).type()),
                () -> assertEquals(BreathingPauseType.NORMAL, result.get(1).type()),
                () -> assertEquals(BreathingPauseType.APNEA, result.get(2).type())
        );
    }
}
