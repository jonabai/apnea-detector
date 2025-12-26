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
import org.springframework.beans.factory.ObjectProvider;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AudioFileSilenceDetectorService Tests")
class AudioFileSilenceDetectorServiceImplTest {

    @Mock
    private SilenceCheckerService silenceCheckerService;

    @Mock
    private WavFileFactory wavFileFactory;

    private AudioFileSilenceDetectorServiceImpl audioFileSilenceDetectorService;
    private AtomicInteger providerCallCount;

    @BeforeEach
    void setUp() {
        providerCallCount = new AtomicInteger(0);
        ObjectProvider<SilenceCheckerService> silenceCheckerProvider = new ObjectProvider<>() {
            @Override
            public SilenceCheckerService getObject() {
                providerCallCount.incrementAndGet();
                return silenceCheckerService;
            }
        };
        audioFileSilenceDetectorService = new AudioFileSilenceDetectorServiceImpl(
                silenceCheckerProvider, wavFileFactory, 2.0f);
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
        when(silenceCheckerService.getCurrentThreshold()).thenReturn(0.00001);

        List<BreathingPause> pauses = audioFileSilenceDetectorService.processFile(filePath);
        assertFalse(pauses.isEmpty());
    }

    @Test
    @DisplayName("Should call calibrate on silence checker")
    void shouldCalibrateOnFileProcess() throws Exception {
        final String filePath = "target/test-classes/example-2.wav";
        WavFile wavFile = new WavFileInputStream(new File(filePath));

        when(wavFileFactory.newWavFile(any())).thenReturn(wavFile);
        when(silenceCheckerService.isSilence(any())).thenReturn(false);
        when(silenceCheckerService.getCurrentThreshold()).thenReturn(0.00001);

        audioFileSilenceDetectorService.processFile(filePath);

        verify(silenceCheckerService).calibrate(any());
        verify(silenceCheckerService).reset();
    }

    @Test
    @DisplayName("Should get fresh silence checker instance for each file")
    void shouldGetFreshInstanceForEachFile() throws Exception {
        final String filePath = "target/test-classes/example-2.wav";

        when(wavFileFactory.newWavFile(any())).thenAnswer(inv -> new WavFileInputStream(new File(filePath)));
        when(silenceCheckerService.isSilence(any())).thenReturn(false);
        when(silenceCheckerService.getCurrentThreshold()).thenReturn(0.00001);

        audioFileSilenceDetectorService.processFile(filePath);
        audioFileSilenceDetectorService.processFile(filePath);

        // Should get a new instance for each file
        assertEquals(2, providerCallCount.get());
    }
}
