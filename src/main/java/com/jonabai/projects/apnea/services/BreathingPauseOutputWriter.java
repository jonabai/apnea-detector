package com.jonabai.projects.apnea.services;


import com.jonabai.projects.apnea.api.domain.BreathingPause;

import java.util.List;

/**
 * A breathing pause output writer
 */
public interface BreathingPauseOutputWriter {

    /**
     * Writes the output for the provided list of breathing pauses
     * @param outputPath output file path
     * @param pauseList list of breathing pauses
     */
    void writeOutput(String outputPath, List<BreathingPause> pauseList);
}
