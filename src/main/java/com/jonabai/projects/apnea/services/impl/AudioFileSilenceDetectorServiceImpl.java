package com.jonabai.projects.apnea.services.impl;


import com.jonabai.projects.apnea.api.domain.BreathingPause;
import com.jonabai.projects.apnea.api.domain.BreathingPauseType;
import com.jonabai.projects.apnea.api.domain.FrameBufferWorkItem;
import com.jonabai.projects.apnea.api.domain.SilenceDetectionException;
import com.jonabai.projects.apnea.services.AudioFileSilenceDetectorService;
import com.jonabai.projects.apnea.services.SilenceCheckerService;
import com.jonabai.projects.apnea.services.WavFile;
import com.jonabai.projects.apnea.services.WavFileFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Service in charge of detecting pauses in an audio file
 */
@Service
public class AudioFileSilenceDetectorServiceImpl implements AudioFileSilenceDetectorService {
    private static final Logger logger = LoggerFactory.getLogger(AudioFileSilenceDetectorService.class);
    private static final float SILENCE_MIN_DURATION = 0.001f; // seconds
    private static final int BUFFER_SIZE =  1024;

    private final SilenceCheckerService silenceDetector;
    private final WavFileFactory wavFileFactory;

    @Autowired
    public AudioFileSilenceDetectorServiceImpl(SilenceCheckerService silenceDetector, WavFileFactory wavFileFactory) {
        this.silenceDetector = silenceDetector;
        this.wavFileFactory = wavFileFactory;
    }

    /**
     * Process one file and returns the list of the detected breathing pauses
     * @param filePath filepath of the file
     * @return the list of breathing pauses
     */
    @Override public List<BreathingPause> processFile(String filePath) {
        File file = new File(filePath);
        if(!file.exists())
            return new ArrayList<>();
        List<BreathingPause> pauseList = new ArrayList<>();
        try(WavFile wavFile = wavFileFactory.newWavFile(file)) {
            displayFileInfo(wavFile);

            // Get the number of audio channels in the wav file
            int numChannels = wavFile.getNumChannels();
            long sampleRate = wavFile.getSampleRate();

            // Create a buffer of "BUFFER_SIZE" frames
            double[] buffer = new double[BUFFER_SIZE * numChannels];

            int framesRead;
            int offset = 0;
            boolean inSilence = false;
            float silenceInit = 0f;
            do {
                // Read frames into buffer
                framesRead = wavFile.readFrames(buffer, BUFFER_SIZE);
                // Loop through frames and check if it is the "silence"
                if(framesRead > 0) {
                    FrameBufferWorkItem workItem = new FrameBufferWorkItem(buffer,
                            inSilence, silenceInit, wavFile, framesRead, sampleRate);
                    processFrameBuffer(workItem, offset, pauseList);
                    inSilence = workItem.isInSilence();
                    silenceInit = workItem.getSilenceInit();
                }
                offset += framesRead;
            }
            while (framesRead != 0);

            if (inSilence)
                checkPossibleBreathingPause(pauseList, wavFile, sampleRate, silenceInit, offset);

        } catch (Exception e) {
            throw new SilenceDetectionException("Error processing file " + filePath, e);
        }
        return pauseList;
    }

    private void processFrameBuffer(FrameBufferWorkItem workItem, int offset,
                                    List<BreathingPause> pauseList) {
        if (silenceDetector.isSilence(workItem.getBuffer())) {
            if (!workItem.isInSilence()) {
                workItem.setInSilence(true);
                workItem.setSilenceInit(offset);
            }
        } else {
            if (workItem.isInSilence()) {
                workItem.setInSilence(false);
                int silenceEnd = offset + workItem.getFramesRead();
                checkPossibleBreathingPause(pauseList,
                        workItem.getWavFile(),
                        workItem.getSampleRate(),
                        workItem.getSilenceInit(),
                        silenceEnd);
            }
        }
    }

    private void checkPossibleBreathingPause(List<BreathingPause> pauseList, WavFile wavFile, long sampleRate, float silenceInit, float silenceEnd) {
        if((silenceEnd - silenceInit / (double)sampleRate) > SILENCE_MIN_DURATION) {
            float secondIni = silenceInit / sampleRate;
            float secondEnd = silenceEnd / sampleRate;
            logger.debug("Silence from {} till {}", secondIni, secondEnd);
            pauseList.add(new BreathingPause(
                    wavFile.getFile().getName(),
                    pauseList.size() + 1,
                    secondIni,
                    secondEnd,
                    BreathingPauseType.NOT_SET));
        }
    }

    private void displayFileInfo(WavFile wavFile) {
        logger.info("File: {}", wavFile.getFile());
        logger.info("Channels: {}, Frames: {}", wavFile.getNumChannels(), wavFile.getNumFrames());
        logger.info("Sample Rate: {}, Block Align: {}", wavFile.getSampleRate(), wavFile.getBlockAlign());
        logger.info("Valid Bits: {}, Bytes per sample: {}", wavFile.getValidBits(), wavFile.getBytesPerSample());
    }

}
