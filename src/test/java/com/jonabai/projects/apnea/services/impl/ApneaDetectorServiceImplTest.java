package com.jonabai.projects.apnea.services.impl;

import com.jonabai.projects.apnea.api.domain.ApneaDetectorServiceException;
import com.jonabai.projects.apnea.services.ApneaDetectorService;
import com.jonabai.projects.apnea.services.AudioFileSilenceDetectorService;
import com.jonabai.projects.apnea.services.BreathingPauseClassificationService;
import com.jonabai.projects.apnea.services.BreathingPauseOutputWriter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ApneaDetectorServiceImplTest {

    @Mock
    private AudioFileSilenceDetectorService audioFileSilenceDetectorService;
    @Mock
    private BreathingPauseClassificationService classificationService;
    @Mock
    private BreathingPauseOutputWriter outputWriter;

    private ApneaDetectorService apneaDetectorService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        apneaDetectorService = new ApneaDetectorServiceImpl(
                audioFileSilenceDetectorService,
                classificationService,
                outputWriter);
    }

    @Test(expected = ApneaDetectorServiceException.class)
    public void processInputFileNull() throws Exception {

        apneaDetectorService.process(null, "Something");

    }

    @Test(expected = ApneaDetectorServiceException.class)
    public void processOutputFileNull() throws Exception {

        apneaDetectorService.process("Some input", null);

    }

    @Test(expected = ApneaDetectorServiceException.class)
    public void processInputFileNotExists() throws Exception {

        apneaDetectorService.process("Not existing file", "some output file");

    }

    @Test
    public void processInputFileEmptyOk() throws Exception {

        apneaDetectorService.process("target/test-classes/bad_input.csv", "some output file");

    }

    @Test
    public void processInputFileOk() throws Exception {

        apneaDetectorService.process("target/test-classes/good_input.csv", "some output file");

    }

}
