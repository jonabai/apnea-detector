package com.jonabai.projects.apnea.services;

/**
 * Apnea detector service
 */
public interface ApneaDetectorService {

    /**
     * Process the contained files into the input csv file and writes the output into the output csv file
     * @param inputCsvPath csv input file
     * @param outputCsvPath csv output file
     */
    void process(String inputCsvPath, String outputCsvPath);
}
