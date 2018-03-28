package com.jonabai.projects.apnea.services.impl;

import com.csvreader.CsvReader;
import com.jonabai.projects.apnea.api.domain.ApneaDetectorServiceException;
import com.jonabai.projects.apnea.api.domain.BreathingPause;
import com.jonabai.projects.apnea.services.ApneaDetectorService;
import com.jonabai.projects.apnea.services.AudioFileSilenceDetectorService;
import com.jonabai.projects.apnea.services.BreathingPauseClassificationService;
import com.jonabai.projects.apnea.services.BreathingPauseOutputWriter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ApneaDetectorService implementation
 */
@Service
public class ApneaDetectorServiceImpl implements ApneaDetectorService {
    private static final Logger logger = LoggerFactory.getLogger(ApneaDetectorServiceImpl.class);
    private final AudioFileSilenceDetectorService audioFileSilenceDetectorService;
    private final BreathingPauseClassificationService classificationService;
    private final BreathingPauseOutputWriter outputWriter;

    public ApneaDetectorServiceImpl(AudioFileSilenceDetectorService audioFileSilenceDetectorService,
                                    BreathingPauseClassificationService classificationService,
                                    BreathingPauseOutputWriter outputWriter) {
        this.audioFileSilenceDetectorService = audioFileSilenceDetectorService;
        this.classificationService = classificationService;
        this.outputWriter = outputWriter;
    }
    @Override
    public void process(String inputCsvPath, String outputCsvPath) {
        if(StringUtils.isEmpty(inputCsvPath))
            throw new ApneaDetectorServiceException("Input file cannot be empty!");
        if(StringUtils.isEmpty(outputCsvPath))
            throw new ApneaDetectorServiceException("Output file cannot be empty!");

        List<String> filePaths = getFilePaths(inputCsvPath);
        List<BreathingPause> pauseList = new ArrayList<>();
        filePaths.stream().parallel().forEach(filePath -> pauseList.addAll(processFile(filePath)));

        outputWriter.writeOutput(outputCsvPath, pauseList);
    }

    private List<BreathingPause> processFile(String inputFilePath) {
        List<BreathingPause> pauseList = audioFileSilenceDetectorService.processFile(inputFilePath);
        logger.info("Detected: {} pauses", pauseList.size());
        classificationService.classify(pauseList);
        return pauseList;
    }

    private List<String> getFilePaths(String inputCsvPath) {
        List<String> filePaths = new ArrayList<>();
        CsvReader reader = null;
        try {
            reader = new CsvReader(inputCsvPath);
            reader.skipLine(); // Skip header
            while(reader.readRecord()) {
                filePaths.add(reader.get(0));
            }
        } catch (IOException e) {
            throw new ApneaDetectorServiceException("Cannot open file " + inputCsvPath, e);
        } finally {
            if(reader != null)
                reader.close();
        }
        return filePaths;
    }
}
