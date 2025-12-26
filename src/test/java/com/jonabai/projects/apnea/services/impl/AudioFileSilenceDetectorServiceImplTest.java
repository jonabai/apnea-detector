package com.jonabai.projects.apnea.services.impl;

import com.jonabai.projects.apnea.api.domain.BreathingPause;
import com.jonabai.projects.apnea.services.SilenceCheckerService;
import com.jonabai.projects.apnea.services.WavFile;
import com.jonabai.projects.apnea.services.WavFileFactory;
import com.jonabai.projects.apnea.services.impl.wav.WavFileInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AudioFileSilenceDetectorService Tests")
class AudioFileSilenceDetectorServiceImplTest {

    @Mock
    private SilenceCheckerService silenceCheckerService;

    @Mock
    private WavFileFactory wavFileFactory;

    private AudioFileSilenceDetectorServiceImpl audioFileSilenceDetectorService;

    @BeforeEach
    void setUp() {
        audioFileSilenceDetectorService = new AudioFileSilenceDetectorServiceImpl(
                silenceCheckerService, wavFileFactory);
    }

    @Test
    @DisplayName("Should return empty list for non-existing file")
    void processNotExistingFileReturnEmptyList() {
        List<BreathingPause> pauses = audioFileSilenceDetectorService.processFile("not_exists.csv");
        assertTrue(pauses.isEmpty());
    }

    @Test
    @DisplayName("Should process WAV file and detect pauses")
    void processFile() throws Exception {
        final String filePath = "target/test-classes/example-2.wav";
        WavFile wavFile = new WavFileInputStream(new File(filePath));

        when(wavFileFactory.newWavFile(any())).thenReturn(wavFile);
        when(silenceCheckerService.isSilence(any())).thenReturn(true);

        List<BreathingPause> pauses = audioFileSilenceDetectorService.processFile(filePath);
        assertFalse(pauses.isEmpty());
    }
}
