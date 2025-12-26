package com.jonabai.projects.apnea.services;

/**
 * Service in charge of silence detection in a group of frames.
 * Supports hysteresis, smoothing, and adaptive threshold calibration.
 */
public interface SilenceCheckerService {

    /**
     * Checks if the group of audio frames represents silence.
     * Uses hysteresis and smoothing for more robust detection.
     * @param buffer audio frames buffer
     * @return if the group represents silence
     */
    boolean isSilence(double[] buffer);

    /**
     * Calibrates the noise floor based on a buffer of audio samples.
     * Should be called with initial "quiet" samples to establish baseline.
     * @param buffer audio frames to use for calibration
     */
    void calibrate(double[] buffer);

    /**
     * Resets the internal state (smoothing history, hysteresis state).
     * Should be called when starting to process a new audio file.
     */
    void reset();

    /**
     * Gets the current adaptive threshold being used.
     * @return the current silence threshold
     */
    double getCurrentThreshold();

    /**
     * Checks if the service has been calibrated.
     * @return true if calibration has been performed
     */
    boolean isCalibrated();
}
