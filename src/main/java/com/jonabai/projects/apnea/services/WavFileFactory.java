package com.jonabai.projects.apnea.services;

import com.jonabai.projects.apnea.api.domain.WavFileException;

import java.io.File;

/**
 * A WavFile factory
 */
public interface WavFileFactory {

    /**
     * Returns a new {@link WavFile} instance ready for reading
     * @param file audio file to be opened
     * @return the {@link WavFile} instance
     * @throws WavFileException in case the instance cannot be created
     */
    WavFile newWavFile(File file) throws WavFileException;
}
