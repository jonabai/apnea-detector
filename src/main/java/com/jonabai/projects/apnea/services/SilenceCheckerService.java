package com.jonabai.projects.apnea.services;

/**
 * Service in charge of silence detection in a group of frames
 */
public interface SilenceCheckerService {

    /**
     * Checks if the group of audio frames
     * @param buffer audio frames buffer
     * @return if the group represents silence
     */
    boolean isSilence(double[] buffer);
}
