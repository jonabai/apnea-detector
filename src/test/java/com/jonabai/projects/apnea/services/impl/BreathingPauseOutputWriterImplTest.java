package com.jonabai.projects.apnea.services.impl;

import com.jonabai.projects.apnea.api.domain.BreathingPause;
import com.jonabai.projects.apnea.api.domain.BreathingPauseType;
import com.jonabai.projects.apnea.api.domain.SilenceDetectionException;
import com.jonabai.projects.apnea.services.BreathingPauseOutputWriter;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class BreathingPauseOutputWriterImplTest {

    private static final String CSV_OUTPUT = "output/test_output.csv";

    private BreathingPauseOutputWriter breathingPauseOutputWriter;

    @Before
    public void setUp() throws Exception {
        breathingPauseOutputWriter = new BreathingPauseOutputWriterImpl();
    }

    @Test
    public void writeOutputToNullOutputFileNotThrowsException() throws Exception {
        breathingPauseOutputWriter.writeOutput(null, new ArrayList<>());
    }

    @Test
    public void writeOutputFromNullListNotThrowsException() throws Exception {
        breathingPauseOutputWriter.writeOutput("something", null);
    }

    @Test(expected = SilenceDetectionException.class)
    public void writeOutputToNotAccessibleFolderThrowsException() throws Exception {
        // given
        List<BreathingPause> pauses = new ArrayList<>();
        pauses.add(new BreathingPause("4", 1, 1f, 1f, BreathingPauseType.NORMAL));
        pauses.add(new BreathingPause("5", 2, 2f, 2f, BreathingPauseType.NORMAL));
        pauses.add(new BreathingPause("6", 3, 3f, 10f, BreathingPauseType.APNEA));

        // then
        breathingPauseOutputWriter.writeOutput("/not_exists/failed.csv", pauses);

    }

    @Test
    public void writeOutputISOk() throws Exception {
        // given
        List<BreathingPause> pauses = new ArrayList<>();
        pauses.add(new BreathingPause("1", 1, 1f, 1f, BreathingPauseType.NORMAL));
        pauses.add(new BreathingPause("2", 2, 2f, 2f, BreathingPauseType.NORMAL));
        pauses.add(new BreathingPause("3", 3, 3f, 10f, BreathingPauseType.APNEA));

        // then
        breathingPauseOutputWriter.writeOutput(CSV_OUTPUT, pauses);

        // then
        File outputFile = new File(CSV_OUTPUT);
        assertTrue(outputFile.exists());
        assertTrue("The files differ!",
                FileUtils.contentEquals(outputFile,
                        new File("target/test-classes/good_output.csv")));

    }

}
