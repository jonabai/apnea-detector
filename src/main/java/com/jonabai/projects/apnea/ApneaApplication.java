package com.jonabai.projects.apnea;

import com.jonabai.projects.apnea.services.ApneaDetectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Instant;

/**
 * Apnea Detector Application.
 * Can run as CLI for batch processing or as a REST API server.
 */
@SpringBootApplication
public class ApneaApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ApneaApplication.class);

    private static final String USAGE_MESSAGE = """
            Usage: java -jar apnea.jar <inputFile.csv> <outputFile.csv>

            Arguments:
              inputFile.csv  - CSV file containing paths to WAV files to analyze
              outputFile.csv - Output CSV file for detected breathing pauses

            For REST API mode, run without arguments.
            """;

    private final ApneaDetectorService apneaDetectorService;

    public ApneaApplication(ApneaDetectorService apneaDetectorService) {
        this.apneaDetectorService = apneaDetectorService;
    }

    public static void main(String[] args) {
        SpringApplication.run(ApneaApplication.class, args);
    }

    @Override
    public void run(String... args) {
        // If no args provided, just run as web server (REST API mode)
        if (args.length == 0) {
            logger.info("Starting in REST API mode. Use POST /api/analyze to analyze WAV files.");
            return;
        }

        if (args.length != 2) {
            logger.error("Invalid number of parameters: expected 2, got {}", args.length);
            logger.info(USAGE_MESSAGE);
            return;
        }

        var startTime = Instant.now();
        logger.info("Starting apnea detection at: {}", startTime);

        apneaDetectorService.process(args[0], args[1]);

        var endTime = Instant.now();
        logger.info("Process completed at: {} (duration: {} ms)",
                endTime,
                endTime.toEpochMilli() - startTime.toEpochMilli());
    }
}
