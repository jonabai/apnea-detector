package com.jonabai.projects.apnea.services.impl;

import com.jonabai.projects.apnea.api.domain.BreathingPause;
import com.jonabai.projects.apnea.api.domain.SilenceDetectionException;
import com.jonabai.projects.apnea.services.AudioFileSilenceDetectorService;
import com.jonabai.projects.apnea.services.SilenceCheckerService;
import com.jonabai.projects.apnea.services.WavFile;
import com.jonabai.projects.apnea.services.WavFileFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for detecting silence pauses in audio files.
 */
@Service
public class AudioFileSilenceDetectorServiceImpl implements AudioFileSilenceDetectorService {

    private static final Logger logger = LoggerFactory.getLogger(AudioFileSilenceDetectorService.class);
    private static final float SILENCE_MIN_DURATION = 0.001f; // seconds
    private static final int BUFFER_SIZE = 1024;

    private final SilenceCheckerService silenceDetector;
    private final WavFileFactory wavFileFactory;

    public AudioFileSilenceDetectorServiceImpl(
            SilenceCheckerService silenceDetector,
            WavFileFactory wavFileFactory) {
        this.silenceDetector = silenceDetector;
        this.wavFileFactory = wavFileFactory;
    }

    @Override
    public List<BreathingPause> processFile(String filePath) {
        var path = Path.of(filePath);
        if (!Files.exists(path)) {
            logger.warn("File does not exist: {}", filePath);
            return List.of();
        }

        List<BreathingPause> pauseList = new ArrayList<>();

        try (var wavFile = wavFileFactory.newWavFile(path.toFile())) {
            logFileInfo(wavFile);
            processWavFile(wavFile, pauseList);
        } catch (Exception e) {
            throw new SilenceDetectionException("Error processing file " + filePath, e);
        }

        return pauseList;
    }

    private void processWavFile(WavFile wavFile, List<BreathingPause> pauseList) throws Exception {
        var numChannels = wavFile.getNumChannels();
        var sampleRate = wavFile.getSampleRate();
        var buffer = new double[BUFFER_SIZE * numChannels];

        var state = new ProcessingState(false, 0f, 0);
        int framesRead;

        while ((framesRead = wavFile.readFrames(buffer, BUFFER_SIZE)) > 0) {
            state = processBuffer(buffer, state, framesRead, wavFile, sampleRate, pauseList);
        }

        // Handle trailing silence
        if (state.inSilence()) {
            addPauseIfValid(pauseList, wavFile, sampleRate, state.silenceInit(), state.currentOffset());
        }
    }

    private ProcessingState processBuffer(
            double[] buffer,
            ProcessingState state,
            int framesRead,
            WavFile wavFile,
            long sampleRate,
            List<BreathingPause> pauseList) {

        var newOffset = state.currentOffset() + framesRead;

        if (silenceDetector.isSilence(buffer)) {
            if (!state.inSilence()) {
                return new ProcessingState(true, state.currentOffset(), newOffset);
            }
            return state.withOffset(newOffset);
        } else {
            if (state.inSilence()) {
                addPauseIfValid(pauseList, wavFile, sampleRate, state.silenceInit(), newOffset);
                return new ProcessingState(false, 0f, newOffset);
            }
            return state.withOffset(newOffset);
        }
    }

    private void addPauseIfValid(
            List<BreathingPause> pauseList,
            WavFile wavFile,
            long sampleRate,
            float silenceInit,
            float silenceEnd) {

        var durationSeconds = (silenceEnd - silenceInit) / sampleRate;

        if (durationSeconds > SILENCE_MIN_DURATION) {
            var startSeconds = silenceInit / sampleRate;
            var endSeconds = silenceEnd / sampleRate;

            logger.debug("Silence from {} to {} seconds", startSeconds, endSeconds);

            pauseList.add(BreathingPause.unclassified(
                    wavFile.getFile().getName(),
                    pauseList.size() + 1,
                    startSeconds,
                    endSeconds
            ));
        }
    }

    private void logFileInfo(WavFile wavFile) {
        logger.info("""
                Processing file: {}
                  Channels: {}, Frames: {}
                  Sample Rate: {}, Block Align: {}
                  Valid Bits: {}, Bytes per sample: {}""",
                wavFile.getFile(),
                wavFile.getNumChannels(), wavFile.getNumFrames(),
                wavFile.getSampleRate(), wavFile.getBlockAlign(),
                wavFile.getValidBits(), wavFile.getBytesPerSample());
    }

    /**
     * Immutable processing state for silence detection.
     */
    private record ProcessingState(boolean inSilence, float silenceInit, int currentOffset) {
        ProcessingState withOffset(int newOffset) {
            return new ProcessingState(inSilence, silenceInit, newOffset);
        }
    }
}
