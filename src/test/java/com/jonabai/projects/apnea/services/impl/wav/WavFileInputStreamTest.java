package com.jonabai.projects.apnea.services.impl.wav;

import com.jonabai.projects.apnea.api.domain.WavFileException;
import com.jonabai.projects.apnea.services.WavFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class WavFileInputStreamTest {

    private WavFile wavFile;

    @Before
    public void setUp() throws Exception {
        wavFile = new WavFileInputStream(new File("target/test-classes/example-2.wav"));
    }

    @After
    public void tearDown() throws Exception {
        wavFile.close();
    }

    @Test(expected = WavFileException.class)
    public void openWavFileWrongFileThrowsException() throws Exception {
        new WavFileInputStream(new File("not exists"));
    }

    @Test
    public void readFrames() throws Exception {

        double[] buffer = new double[1024];
        int numFrames = wavFile.readFrames(buffer, 1024);

        assertTrue(numFrames > 0);
    }

}
