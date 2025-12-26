package com.jonabai.projects.apnea.services.impl;

import com.jonabai.projects.apnea.services.SilenceCheckerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * SilenceCheckerService implementation using RMS (Root Mean Square) calculation.
 */
@Service
public class SilenceCheckerServiceImpl implements SilenceCheckerService {

    private final double silenceThreshold;

    public SilenceCheckerServiceImpl(
            @Value("${apnea.silence.checker.threshold:0.00001}") double silenceThreshold) {
        this.silenceThreshold = silenceThreshold;
    }

    @Override
    public boolean isSilence(double[] buffer) {
        return buffer != null && buffer.length > 0 && volumeRMS(buffer) <= silenceThreshold;
    }

    private double volumeRMS(double[] buffer) {
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
