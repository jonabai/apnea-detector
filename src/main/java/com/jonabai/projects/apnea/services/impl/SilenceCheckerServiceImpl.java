package com.jonabai.projects.apnea.services.impl;

import com.jonabai.projects.apnea.services.SilenceCheckerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * Enhanced SilenceCheckerService implementation with:
 * - RMS (Root Mean Square) calculation for volume measurement
 * - Hysteresis (dual thresholds) to prevent state flutter
 * - Moving average smoothing to reduce noise sensitivity
 * - Adaptive threshold calibration based on initial noise floor
 */
@Service
@Scope("prototype")
public class SilenceCheckerServiceImpl implements SilenceCheckerService {

    private final double baseSilenceThreshold;
    private final double hysteresisMultiplier;
    private final int smoothingWindowSize;
    private final double calibrationMultiplier;

    private final Deque<Double> rmsHistory;
    private double adaptiveThreshold;
    private boolean inSilenceState;
    private boolean calibrated;

    public SilenceCheckerServiceImpl(
            @Value("${apnea.silence.checker.threshold:0.00001}") double baseSilenceThreshold,
            @Value("${apnea.silence.checker.hysteresis.multiplier:5.0}") double hysteresisMultiplier,
            @Value("${apnea.silence.checker.smoothing.window:5}") int smoothingWindowSize,
            @Value("${apnea.silence.checker.calibration.multiplier:2.0}") double calibrationMultiplier) {
        this.baseSilenceThreshold = baseSilenceThreshold;
        this.hysteresisMultiplier = hysteresisMultiplier;
        this.smoothingWindowSize = smoothingWindowSize;
        this.calibrationMultiplier = calibrationMultiplier;
        this.rmsHistory = new ArrayDeque<>(smoothingWindowSize);
        this.adaptiveThreshold = baseSilenceThreshold;
        this.inSilenceState = false;
        this.calibrated = false;
    }

    @Override
    public boolean isSilence(double[] buffer) {
        if (buffer == null || buffer.length == 0) {
            return false;
        }

        double currentRms = volumeRMS(buffer);
        double smoothedRms = updateAndGetSmoothedRms(currentRms);

        // Hysteresis: use different thresholds for entering vs exiting silence
        double enterThreshold = adaptiveThreshold;
        double exitThreshold = adaptiveThreshold * hysteresisMultiplier;

        if (inSilenceState) {
            // Currently in silence - need higher RMS to exit
            if (smoothedRms > exitThreshold) {
                inSilenceState = false;
            }
        } else {
            // Currently in sound - need lower RMS to enter silence
            if (smoothedRms <= enterThreshold) {
                inSilenceState = true;
            }
        }

        return inSilenceState;
    }

    @Override
    public void calibrate(double[] buffer) {
        if (buffer == null || buffer.length == 0) {
            return;
        }

        double noiseFloorRms = volumeRMS(buffer);

        // Set adaptive threshold based on noise floor
        // Use calibration multiplier to set threshold above the noise floor
        adaptiveThreshold = Math.max(baseSilenceThreshold, noiseFloorRms * calibrationMultiplier);
        calibrated = true;
    }

    @Override
    public void reset() {
        rmsHistory.clear();
        inSilenceState = false;
        // Keep calibrated threshold if already calibrated
    }

    @Override
    public double getCurrentThreshold() {
        return adaptiveThreshold;
    }

    @Override
    public boolean isCalibrated() {
        return calibrated;
    }

    private double updateAndGetSmoothedRms(double currentRms) {
        rmsHistory.addLast(currentRms);

        // Keep only the last N values
        while (rmsHistory.size() > smoothingWindowSize) {
            rmsHistory.removeFirst();
        }

        // Calculate moving average
        return rmsHistory.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(currentRms);
    }

    double volumeRMS(double[] buffer) {
        if (buffer.length == 0) {
            return Double.MAX_VALUE;
        }

        var average = Arrays.stream(buffer).average().orElse(0.0);

        var meanSquareSum = Arrays.stream(buffer)
                .map(value -> Math.pow(value - average, 2))
                .sum();

        return Math.sqrt(meanSquareSum / buffer.length);
    }
}
