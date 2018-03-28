package com.jonabai.projects.apnea.services;

import com.jonabai.projects.apnea.api.domain.BreathingPause;

import java.util.List;

/**
 * Service in charge of detecting pauses in an audio file
 */
public interface AudioFileSilenceDetectorService {

    /**
     * Process one file and returns the list of the detected breathing pauses
     * @param filePath filepath of the file
     * @return the list of breathing pauses
     */
    List<BreathingPause> processFile(String filePath);
}
