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
                new BreathingPause("test.wav", 1, 1f, 2f, BreathingPauseType.NORMAL),
                new BreathingPause("test.wav", 2, 5f, 20f, BreathingPauseType.MILD_APNEA),
                new BreathingPause("test.wav", 3, 30f, 65f, BreathingPauseType.SEVERE_APNEA)
        );

        var outputPath = tempDir.resolve("test_output.csv");

        // when
        outputWriter.writeOutput(outputPath.toString(), pauses);

        // then
        assertTrue(Files.exists(outputPath));

        var lines = Files.readAllLines(outputPath);
        assertEquals(4, lines.size()); // header + 3 data rows

        // Check header
        assertEquals("File Path,Pause #,start [secs],end [secs],duration [secs],type", lines.get(0));

        // Check data rows
        assertTrue(lines.get(1).contains("NORMAL"));
        assertTrue(lines.get(2).contains("MILD_APNEA"));
        assertTrue(lines.get(3).contains("SEVERE_APNEA"));
    }
}
