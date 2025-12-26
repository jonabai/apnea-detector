package com.jonabai.projects.apnea.services.impl;

import com.jonabai.projects.apnea.api.domain.BreathingPause;
import com.jonabai.projects.apnea.api.domain.BreathingPauseType;
import com.jonabai.projects.apnea.api.domain.SilenceDetectionException;
import com.jonabai.projects.apnea.services.BreathingPauseOutputWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BreathingPauseOutputWriter Tests")
class BreathingPauseOutputWriterImplTest {

    private BreathingPauseOutputWriter outputWriter;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        outputWriter = new BreathingPauseOutputWriterImpl();
    }

    @Test
    @DisplayName("Should handle null output path gracefully")
    void writeOutputToNullOutputFileNotThrowsException() {
        assertDoesNotThrow(() -> outputWriter.writeOutput(null, new ArrayList<>()));
    }

    @Test
    @DisplayName("Should handle null pause list gracefully")
    void writeOutputFromNullListNotThrowsException() {
        assertDoesNotThrow(() -> outputWriter.writeOutput("something", null));
    }

    @Test
    @DisplayName("Should throw exception for inaccessible directory")
    void writeOutputToNotAccessibleFolderThrowsException() {
        var pauses = List.of(
                new BreathingPause("4", 1, 1f, 1f, BreathingPauseType.NORMAL)
        );

        assertThrows(SilenceDetectionException.class,
                () -> outputWriter.writeOutput("/not_exists/failed.csv", pauses));
    }

    @Test
    @DisplayName("Should write output CSV correctly")
    void writeOutputIsOk() throws IOException {
        // given
        var pauses = List.of(
                new BreathingPause("1", 1, 1f, 1f, BreathingPauseType.NORMAL),
                new BreathingPause("2", 2, 2f, 2f, BreathingPauseType.NORMAL),
                new BreathingPause("3", 3, 3f, 10f, BreathingPauseType.APNEA)
        );

        var outputPath = tempDir.resolve("test_output.csv");

        // when
        outputWriter.writeOutput(outputPath.toString(), pauses);

        // then
        assertTrue(Files.exists(outputPath));

        var expectedPath = Path.of("target/test-classes/good_output.csv");

        // Use Files.mismatch() instead of FileUtils.contentEquals()
        assertEquals(-1L, Files.mismatch(outputPath, expectedPath),
                "Output file content should match expected");
    }
}
