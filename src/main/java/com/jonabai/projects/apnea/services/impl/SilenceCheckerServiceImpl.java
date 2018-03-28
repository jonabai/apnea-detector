package com.jonabai.projects.apnea.services.impl;

import com.jonabai.projects.apnea.services.SilenceCheckerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * SilenceCheckerService implemented using rms
 */
@Service
public class SilenceCheckerServiceImpl implements SilenceCheckerService {

    private double silenceThreshold;

    public SilenceCheckerServiceImpl(@Value("${apnea.silence.checker.threshold:0.00001}") double silenceThreshold) {
        this.silenceThreshold = silenceThreshold;
    }

    public boolean isSilence(double[] buffer) {
        return buffer != null && volumeRMS(buffer) <= silenceThreshold;
    }

    private double volumeRMS(double[] buffer) {
        double sum = 0d;
        if (buffer.length==0) {
            return Double.MAX_VALUE;
        } else {
            for (double aBuffer : buffer) {
                sum += aBuffer;
            }
        }
        double average = sum/buffer.length;

        double sumMeanSquare = 0d;
        for (double aBuffer : buffer) {
            sumMeanSquare += Math.pow(aBuffer - average, 2d);
        }
        double averageMeanSquare = sumMeanSquare/buffer.length;

        return Math.sqrt(averageMeanSquare);
    }
}
