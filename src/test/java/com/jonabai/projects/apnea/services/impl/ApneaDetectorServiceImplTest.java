package com.jonabai.projects.apnea.services.impl;

import com.jonabai.projects.apnea.api.domain.ApneaDetectorServiceException;
import com.jonabai.projects.apnea.services.ApneaDetectorService;
import com.jonabai.projects.apnea.services.AudioFileSilenceDetectorService;
import com.jonabai.projects.apnea.services.BreathingPauseClassificationService;
import com.jonabai.projects.apnea.services.BreathingPauseOutputWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApneaDetectorService Tests")
class ApneaDetectorServiceImplTest {

    @Mock
    private AudioFileSilenceDetectorService audioFileSilenceDetectorService;

    @Mock
    private BreathingPauseClassificationService classificationService;

    @Mock
    private BreathingPauseOutputWriter outputWriter;

    private ApneaDetectorService apneaDetectorService;

    @BeforeEach
    void setUp() {
        apneaDetectorService = new ApneaDetectorServiceImpl(
                audioFileSilenceDetectorService,
                classificationService,
                outputWriter);
    }

    @Nested
    @DisplayName("Input Validation Tests")
    class InputValidationTests {

        @Test
        @DisplayName("Should throw exception when input file is null")
        void processInputFileNull() {
            assertThrows(ApneaDetectorServiceException.class,
                    () -> apneaDetectorService.process(null, "Something"));
        }

        @Test
        @DisplayName("Should throw exception when output file is null")
        void processOutputFileNull() {
            assertThrows(ApneaDetectorServiceException.class,
                    () -> apneaDetectorService.process("Some input", null));
        }

        @Test
        @DisplayName("Should throw exception when input file does not exist")
        void processInputFileNotExists() {
            assertThrows(ApneaDetectorServiceException.class,
                    () -> apneaDetectorService.process("Not existing file", "some output file"));
        }
    }

    @Nested
    @DisplayName("Processing Tests")
    class ProcessingTests {

        @Test
        @DisplayName("Should process empty input file successfully")
        void processInputFileEmptyOk() {
            when(classificationService.classify(anyList())).thenReturn(List.of());

            assertDoesNotThrow(() ->
                    apneaDetectorService.process("target/test-classes/bad_input.csv", "some output file"));
        }

        @Test
        @DisplayName("Should process valid input file successfully")
        void processInputFileOk() {
            when(audioFileSilenceDetectorService.processFile(anyString())).thenReturn(List.of());
            when(classificationService.classify(anyList())).thenReturn(List.of());

            assertDoesNotThrow(() ->
                    apneaDetectorService.process("target/test-classes/good_input.csv", "some output file"));
        }
    }
}
