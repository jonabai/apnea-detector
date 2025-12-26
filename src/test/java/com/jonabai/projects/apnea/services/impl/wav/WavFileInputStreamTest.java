package com.jonabai.projects.apnea.services.impl.wav;

import com.jonabai.projects.apnea.api.domain.WavFileException;
import com.jonabai.projects.apnea.services.WavFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WavFileInputStream Tests")
class WavFileInputStreamTest {

    private WavFile wavFile;

    @BeforeEach
    void setUp() throws Exception {
        wavFile = new WavFileInputStream(new File("target/test-classes/example-2.wav"));
    }

    @AfterEach
    void tearDown() throws Exception {
        wavFile.close();
    }

    @Test
    @DisplayName("Should throw exception for non-existing file")
    void openWavFileWrongFileThrowsException() {
        assertThrows(WavFileException.class,
                () -> new WavFileInputStream(new File("not exists")));
    }

    @Test
    @DisplayName("Should read frames from WAV file")
    void readFrames() throws Exception {
        var buffer = new double[1024];
        var numFrames = wavFile.readFrames(buffer, 1024);

        assertTrue(numFrames > 0);
    }
}
