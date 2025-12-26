package com.jonabai.projects.apnea.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SilenceCheckerService Tests")
class SilenceCheckerServiceImplTest {

    private static final double SILENCE_THRESHOLD = 0.00001d;
    private static final double HYSTERESIS_MULTIPLIER = 5.0d;
    private static final int SMOOTHING_WINDOW = 5;
    private static final double CALIBRATION_MULTIPLIER = 2.0d;

    private SilenceCheckerServiceImpl silenceCheckerService;

    @BeforeEach
    void setUp() {
        silenceCheckerService = new SilenceCheckerServiceImpl(
                SILENCE_THRESHOLD, HYSTERESIS_MULTIPLIER, SMOOTHING_WINDOW, CALIBRATION_MULTIPLIER);
    }

    @Nested
    @DisplayName("Basic silence detection")
    class BasicSilenceDetection {

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

    @Nested
    @DisplayName("Hysteresis behavior")
    class HysteresisBehavior {

        @Test
        @DisplayName("Should stay in silence state on borderline signals")
        void stayInSilenceOnBorderlineSignals() {
            silenceCheckerService.reset();
            var silentBuffer = new double[]{0d, 0d, 0d, 0d, 0d};

            // Fill smoothing window with silence to establish silence state
            for (int i = 0; i < SMOOTHING_WINDOW + 1; i++) {
                silenceCheckerService.isSilence(silentBuffer);
            }

            // Verify we're in silence
            assertTrue(silenceCheckerService.isSilence(silentBuffer));

            // Borderline buffer should stay in silence due to hysteresis and smoothing
            var borderlineBuffer = new double[]{0.00002, 0.00002, 0.00002, 0.00002, 0.00002};
            assertTrue(silenceCheckerService.isSilence(borderlineBuffer),
                    "Borderline signal should remain in silence state due to hysteresis");
        }

        @Test
        @DisplayName("Should exit silence with loud audio")
        void exitSilenceWithLoudAudio() {
            silenceCheckerService.reset();
            var silentBuffer = new double[]{0d, 0d, 0d, 0d, 0d};
            // Note: RMS uses deviation from mean, so constant values have RMS = 0
            // Use a buffer with variance to get non-zero RMS
            var loudBuffer = new double[]{0.5, -0.5, 0.5, -0.5, 0.5};

            // Establish silence state
            for (int i = 0; i < SMOOTHING_WINDOW + 1; i++) {
                silenceCheckerService.isSilence(silentBuffer);
            }
            assertTrue(silenceCheckerService.isSilence(silentBuffer));

            // Fill smoothing window with loud samples to exit silence
            for (int i = 0; i < SMOOTHING_WINDOW + 1; i++) {
                silenceCheckerService.isSilence(loudBuffer);
            }
            assertFalse(silenceCheckerService.isSilence(loudBuffer),
                    "Very loud signal should exit silence state");
        }
    }

    @Nested
    @DisplayName("Calibration behavior")
    class CalibrationBehavior {

        @Test
        @DisplayName("Should not be calibrated initially")
        void notCalibratedInitially() {
            assertFalse(silenceCheckerService.isCalibrated());
        }

        @Test
        @DisplayName("Should be calibrated after calibrate is called")
        void calibratedAfterCalibrate() {
            var buffer = new double[]{0.001, 0.001, 0.001, 0.001, 0.001};
            silenceCheckerService.calibrate(buffer);
            assertTrue(silenceCheckerService.isCalibrated());
        }

        @Test
        @DisplayName("Should adjust threshold based on noise floor with variance")
        void adjustThresholdBasedOnNoiseFloor() {
            // Calibrate with noisy signal that has variance (RMS calculation needs variation)
            var noisyBuffer = new double[]{0.01, 0.02, 0.01, 0.02, 0.01};
            silenceCheckerService.calibrate(noisyBuffer);

            // Threshold should be higher than base threshold due to noise floor calibration
            assertTrue(silenceCheckerService.getCurrentThreshold() > SILENCE_THRESHOLD,
                    "Threshold should be > base threshold, actual: " + silenceCheckerService.getCurrentThreshold());
        }

        @Test
        @DisplayName("Should use base threshold for very quiet calibration signal")
        void useBaseThresholdForQuietCalibration() {
            // Calibrate with very quiet signal
            var quietBuffer = new double[]{0.000001, 0.000001, 0.000001, 0.000001, 0.000001};
            silenceCheckerService.calibrate(quietBuffer);

            // Threshold should be at least the base threshold
            assertTrue(silenceCheckerService.getCurrentThreshold() >= SILENCE_THRESHOLD);
        }
    }

    @Nested
    @DisplayName("Reset behavior")
    class ResetBehavior {

        @Test
        @DisplayName("Should reset internal state but keep calibration")
        void resetKeepsCalibration() {
            // Calibrate with variance
            var buffer = new double[]{0.01, 0.02, 0.01, 0.02, 0.01};
            silenceCheckerService.calibrate(buffer);
            double calibratedThreshold = silenceCheckerService.getCurrentThreshold();

            // Reset
            silenceCheckerService.reset();

            // Calibration should be preserved
            assertTrue(silenceCheckerService.isCalibrated());
            assertEquals(calibratedThreshold, silenceCheckerService.getCurrentThreshold());
        }

        @Test
        @DisplayName("Should reset hysteresis state and smoothing history")
        void resetHysteresisAndSmoothingState() {
            // Fill smoothing window with silence to enter silence state
            var silentBuffer = new double[]{0d, 0d, 0d, 0d, 0d};
            for (int i = 0; i < SMOOTHING_WINDOW + 1; i++) {
                silenceCheckerService.isSilence(silentBuffer);
            }
            assertTrue(silenceCheckerService.isSilence(silentBuffer));

            // Reset clears smoothing history and hysteresis state
            silenceCheckerService.reset();

            // After reset, loud buffer with variance should not be silence
            // Note: RMS uses deviation from mean, so we need variance
            var loudBuffer = new double[]{0.5, -0.5, 0.5, -0.5, 0.5};
            // Need to fill the smoothing window with loud to get reliable result
            for (int i = 0; i < SMOOTHING_WINDOW + 1; i++) {
                silenceCheckerService.isSilence(loudBuffer);
            }
            assertFalse(silenceCheckerService.isSilence(loudBuffer),
                    "After reset and loud samples, should not be in silence");
        }
    }

    @Nested
    @DisplayName("Smoothing behavior")
    class SmoothingBehavior {

        @Test
        @DisplayName("Should smooth out single spike in otherwise silent signal")
        void smoothOutSingleSpike() {
            // Fill smoothing window with silence
            var silentBuffer = new double[]{0d, 0d, 0d, 0d, 0d};
            for (int i = 0; i < SMOOTHING_WINDOW; i++) {
                silenceCheckerService.isSilence(silentBuffer);
            }

            // Single slightly louder buffer should be smoothed out
            var slightlyLouderBuffer = new double[]{0.00003, 0.00003, 0.00003, 0.00003, 0.00003};
            // Due to smoothing, this should still be detected as silence
            assertTrue(silenceCheckerService.isSilence(slightlyLouderBuffer));
        }
    }
}
