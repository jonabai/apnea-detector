package com.jonabai.projects.apnea.services.impl;

import com.jonabai.projects.apnea.api.domain.ApneaDetectorServiceException;
import com.jonabai.projects.apnea.api.domain.BreathingPause;
import com.jonabai.projects.apnea.services.ApneaDetectorService;
import com.jonabai.projects.apnea.services.AudioFileSilenceDetectorService;
import com.jonabai.projects.apnea.services.BreathingPauseClassificationService;
import com.jonabai.projects.apnea.services.BreathingPauseOutputWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * ApneaDetectorService implementation for batch processing.
 */
@Service
public class ApneaDetectorServiceImpl implements ApneaDetectorService {

    private static final Logger logger = LoggerFactory.getLogger(ApneaDetectorServiceImpl.class);

    private final AudioFileSilenceDetectorService audioFileSilenceDetectorService;
    private final BreathingPauseClassificationService classificationService;
    private final BreathingPauseOutputWriter outputWriter;

    public ApneaDetectorServiceImpl(
            AudioFileSilenceDetectorService audioFileSilenceDetectorService,
            BreathingPauseClassificationService classificationService,
            BreathingPauseOutputWriter outputWriter) {
        this.audioFileSilenceDetectorService = audioFileSilenceDetectorService;
        this.classificationService = classificationService;
        this.outputWriter = outputWriter;
    }

    @Override
    public void process(String inputCsvPath, String outputCsvPath) {
        validateInput(inputCsvPath, "Input file");
        validateInput(outputCsvPath, "Output file");

        var filePaths = getFilePaths(inputCsvPath);

        // Use synchronized list for thread-safe parallel processing
        List<BreathingPause> pauseList = Collections.synchronizedList(new ArrayList<>());

        filePaths.parallelStream()
                .map(this::processFile)
                .forEach(pauseList::addAll);

        outputWriter.writeOutput(outputCsvPath, pauseList);
    }

    private void validateInput(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ApneaDetectorServiceException("%s cannot be empty!".formatted(fieldName));
        }
    }

    private List<BreathingPause> processFile(String inputFilePath) {
        var pauseList = audioFileSilenceDetectorService.processFile(inputFilePath);
        logger.info("Detected: {} pauses in {}", pauseList.size(), inputFilePath);
        return classificationService.classify(pauseList);
    }

    private List<String> getFilePaths(String inputCsvPath) {
        var inputPath = Path.of(inputCsvPath);

        try (Reader reader = Files.newBufferedReader(inputPath);
             CSVParser csvParser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .build()
                     .parse(reader)) {

            return csvParser.stream()
                    .map(record -> record.get(0))
                    .filter(Objects::nonNull)
                    .filter(path -> !path.isBlank())
                    .toList();

        } catch (IOException e) {
            throw new ApneaDetectorServiceException("Cannot open file " + inputCsvPath, e);
        }
    }
}
