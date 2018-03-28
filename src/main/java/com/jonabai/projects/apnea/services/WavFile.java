package com.jonabai.projects.apnea.services;

import com.jonabai.projects.apnea.api.domain.WavFileException;

import java.io.File;
import java.io.IOException;

/**
 * Abstracts a WavFile implementation
 */
public interface WavFile extends AutoCloseable {

    /**
     * Gets the number of audio channels
     * @return the number of audio channels
     */
    int getNumChannels();

    /**
     * Gets the number of frames within the data section
     * @return the number of frames within the data section
     */
    long getNumFrames();

    /**
     * Gets the sample rate of the audio file
     * @return the sample rate
     */
    long getSampleRate();

    /**
     * Gets the valid bits
     * @return the valid bits
     */
    int getValidBits();

    /**
     * Gets the bytes per sample
     * @return the bytes per sample
     */
    int getBytesPerSample();

    /**
     * Gets the block align
     * @return the block align
     */
    int getBlockAlign();

    /**
     * Gets the file represented by this WavFile instance
     * @return the file represented by this WavFile instance
     */
    File getFile();

    /**
     * Reads the number of frames from teh current position to the provided buffer
     * @param sampleBuffer buffer to be written
     * @param numFramesToRead number of frames to be read
     * @return the final number of frames read
     * @throws WavFileException in case something went wrong
     */
    int readFrames(double[] sampleBuffer, int numFramesToRead) throws WavFileException;

    int readFrames(double[] sampleBuffer, int offset, int numFramesToRead) throws IOException, WavFileException;

    /**
     * Closes the file for reading
     * @throws IOException in case something wen wrong
     */
    @Override
    void close() throws IOException;
}
