package com.jonabai.projects.apnea.services.impl;

import com.jonabai.projects.apnea.api.domain.BreathingPause;
import com.jonabai.projects.apnea.api.domain.BreathingPauseType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BreathingPauseClassificationService Tests")
class BreathingPauseClassificationServiceImplTest {

    private static final float HYPOPNEA_THRESHOLD = 3.0f;
    private static final float MILD_THRESHOLD = 10.0f;
    private static final float MODERATE_THRESHOLD = 20.0f;
    private static final float SEVERE_THRESHOLD = 30.0f;

    private BreathingPauseClassificationServiceImpl classificationService;

    @BeforeEach
    void setUp() {
        classificationService = new BreathingPauseClassificationServiceImpl(
                HYPOPNEA_THRESHOLD, MILD_THRESHOLD, MODERATE_THRESHOLD, SEVERE_THRESHOLD);
    }

    @Nested
    @DisplayName("Empty input handling")
    class EmptyInputHandling {

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
    }

    @Nested
    @DisplayName("Severity gradation classification")
    class SeverityGradation {

        @ParameterizedTest
        @DisplayName("Should classify pauses correctly based on duration with severity levels")
        @CsvSource({
                "0.0, 1.0, NORMAL",
                "0.0, 2.9, NORMAL",
                "0.0, 3.0, HYPOPNEA",
                "0.0, 9.9, HYPOPNEA",
                "0.0, 10.0, MILD_APNEA",
                "0.0, 19.9, MILD_APNEA",
                "0.0, 20.0, MODERATE_APNEA",
                "0.0, 29.9, MODERATE_APNEA",
                "0.0, 30.0, SEVERE_APNEA",
                "0.0, 60.0, SEVERE_APNEA"
        })
        void classifyBasedOnDurationWithSeverity(float start, float end, BreathingPauseType expectedType) {
            var pause = BreathingPause.unclassified("test.wav", 1, start, end);

            var result = classificationService.classify(List.of(pause));

            assertEquals(1, result.size());
            assertEquals(expectedType, result.get(0).type());
        }

        @Test
        @DisplayName("Should classify multiple pauses with different severities")
        void classifyMultiplePausesWithSeverity() {
            var pauses = List.of(
                    BreathingPause.unclassified("1", 1, 0f, 1f),      // NORMAL
                    BreathingPause.unclassified("2", 2, 0f, 5f),      // HYPOPNEA
                    BreathingPause.unclassified("3", 3, 0f, 15f),     // MILD_APNEA
                    BreathingPause.unclassified("4", 4, 0f, 25f),     // MODERATE_APNEA
                    BreathingPause.unclassified("5", 5, 0f, 45f)      // SEVERE_APNEA
            );

            var result = classificationService.classify(pauses);

            assertAll(
                    () -> assertEquals(BreathingPauseType.NORMAL, result.get(0).type()),
                    () -> assertEquals(BreathingPauseType.HYPOPNEA, result.get(1).type()),
                    () -> assertEquals(BreathingPauseType.MILD_APNEA, result.get(2).type()),
                    () -> assertEquals(BreathingPauseType.MODERATE_APNEA, result.get(3).type()),
                    () -> assertEquals(BreathingPauseType.SEVERE_APNEA, result.get(4).type())
            );
        }
    }

    @Nested
    @DisplayName("Threshold configuration")
    class ThresholdConfiguration {

        @Test
        @DisplayName("Should return configured thresholds")
        void getConfiguredThresholds() {
            assertEquals(HYPOPNEA_THRESHOLD, classificationService.getHypopneaThresholdSeconds());
            assertEquals(MILD_THRESHOLD, classificationService.getMildApneaThresholdSeconds());
            assertEquals(MODERATE_THRESHOLD, classificationService.getModerateApneaThresholdSeconds());
            assertEquals(SEVERE_THRESHOLD, classificationService.getSevereApneaThresholdSeconds());
        }

        @Test
        @DisplayName("Should classify with custom thresholds")
        void classifyWithCustomThresholds() {
            // Create service with custom thresholds
            var customService = new BreathingPauseClassificationServiceImpl(2f, 5f, 10f, 15f);

            var pauses = List.of(
                    BreathingPause.unclassified("1", 1, 0f, 1f),    // NORMAL (< 2)
                    BreathingPause.unclassified("2", 2, 0f, 3f),    // HYPOPNEA (>= 2, < 5)
                    BreathingPause.unclassified("3", 3, 0f, 7f),    // MILD_APNEA (>= 5, < 10)
                    BreathingPause.unclassified("4", 4, 0f, 12f),   // MODERATE_APNEA (>= 10, < 15)
                    BreathingPause.unclassified("5", 5, 0f, 20f)    // SEVERE_APNEA (>= 15)
            );

            var result = customService.classify(pauses);

            assertAll(
                    () -> assertEquals(BreathingPauseType.NORMAL, result.get(0).type()),
                    () -> assertEquals(BreathingPauseType.HYPOPNEA, result.get(1).type()),
                    () -> assertEquals(BreathingPauseType.MILD_APNEA, result.get(2).type()),
                    () -> assertEquals(BreathingPauseType.MODERATE_APNEA, result.get(3).type()),
                    () -> assertEquals(BreathingPauseType.SEVERE_APNEA, result.get(4).type())
            );
        }
    }

    @Nested
    @DisplayName("Health concern indicators")
    class HealthConcernIndicators {

        @Test
        @DisplayName("Should identify health concerns correctly")
        void identifyHealthConcerns() {
            assertFalse(BreathingPauseType.NORMAL.isHealthConcern());
            assertFalse(BreathingPauseType.HYPOPNEA.isHealthConcern());
            assertTrue(BreathingPauseType.MILD_APNEA.isHealthConcern());
            assertTrue(BreathingPauseType.MODERATE_APNEA.isHealthConcern());
            assertTrue(BreathingPauseType.SEVERE_APNEA.isHealthConcern());
        }

        @Test
        @DisplayName("Should identify abnormal breathing correctly")
        void identifyAbnormalBreathing() {
            assertFalse(BreathingPauseType.NORMAL.isAbnormal());
            assertTrue(BreathingPauseType.HYPOPNEA.isAbnormal());
            assertTrue(BreathingPauseType.MILD_APNEA.isAbnormal());
            assertTrue(BreathingPauseType.MODERATE_APNEA.isAbnormal());
            assertTrue(BreathingPauseType.SEVERE_APNEA.isAbnormal());
        }
    }
}
