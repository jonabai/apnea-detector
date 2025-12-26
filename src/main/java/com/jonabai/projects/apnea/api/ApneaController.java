package com.jonabai.projects.apnea.api;

import com.jonabai.projects.apnea.api.dto.AnalysisResponse;
import com.jonabai.projects.apnea.services.AudioFileSilenceDetectorService;
import com.jonabai.projects.apnea.services.BreathingPauseClassificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * REST API controller for apnea detection.
 */
@RestController
@RequestMapping("/api")
public class ApneaController {

    private static final Logger logger = LoggerFactory.getLogger(ApneaController.class);

    private final AudioFileSilenceDetectorService silenceDetectorService;
    private final BreathingPauseClassificationService classificationService;

    public ApneaController(
            AudioFileSilenceDetectorService silenceDetectorService,
            BreathingPauseClassificationService classificationService) {
        this.silenceDetectorService = silenceDetectorService;
        this.classificationService = classificationService;
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    /**
     * Analyze a WAV file for breathing pauses and potential apnea events.
     *
     * @param file the WAV file to analyze
     * @return analysis results with detected pauses
     */
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalysisResponse> analyze(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".wav")) {
            return ResponseEntity.badRequest().build();
        }

        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("apnea-", ".wav");
            file.transferTo(tempFile);

            logger.info("Analyzing file: {}", originalFilename);

            var pauses = silenceDetectorService.processFile(tempFile.toString());
            var classifiedPauses = classificationService.classify(pauses);

            var response = AnalysisResponse.from(originalFilename, classifiedPauses);

            logger.info("Analysis complete: {} pauses detected ({} apnea events)",
                    response.totalPauses(), response.apneaCount());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            logger.error("Error processing file: {}", originalFilename, e);
            return ResponseEntity.internalServerError().build();
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    logger.warn("Failed to delete temp file: {}", tempFile, e);
                }
            }
        }
    }
}
