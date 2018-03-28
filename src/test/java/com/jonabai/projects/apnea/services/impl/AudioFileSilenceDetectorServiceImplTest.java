package com.jonabai.projects.apnea.services.impl;

import com.jonabai.projects.apnea.api.domain.BreathingPause;
import com.jonabai.projects.apnea.services.SilenceCheckerService;
import com.jonabai.projects.apnea.services.WavFile;
import com.jonabai.projects.apnea.services.WavFileFactory;
import com.jonabai.projects.apnea.services.impl.wav.WavFileInputStream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class AudioFileSilenceDetectorServiceImplTest {

    @Mock
    private SilenceCheckerService silenceCheckerService;

    @Mock
    private WavFileFactory wavFileFactory;

    private AudioFileSilenceDetectorServiceImpl audioFileSilenceDetectorService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        audioFileSilenceDetectorService = new AudioFileSilenceDetectorServiceImpl(silenceCheckerService, wavFileFactory);
    }

    @Test
    public void processNotExistingFileReturnEmptyList() throws Exception {
        List<BreathingPause> pauses = audioFileSilenceDetectorService.processFile("not_exists.csv");
        assertTrue(pauses.isEmpty());
    }

    @Test
    public void processFile() throws Exception {
        final String filePath = "target/test-classes/example-2.wav";
        WavFile wavFile = new WavFileInputStream(new File(filePath));

        when(wavFileFactory.newWavFile(any())).thenReturn(wavFile);
        when(silenceCheckerService.isSilence(any())).thenReturn(true);

        List<BreathingPause> pauses = audioFileSilenceDetectorService.processFile(filePath);
        assertFalse(pauses.isEmpty());
    }
}
