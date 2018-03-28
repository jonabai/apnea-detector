package com.jonabai.projects.apnea.services.impl;

import com.jonabai.projects.apnea.api.domain.WavFileException;
import com.jonabai.projects.apnea.services.WavFile;
import com.jonabai.projects.apnea.services.WavFileFactory;
import com.jonabai.projects.apnea.services.impl.wav.WavFileInputStream;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * WavFile Factory implementation returning WavFileInputStream
 */
@Service
public class WavFileInputStreamFactoryImpl implements WavFileFactory {

    @Override
    public WavFile newWavFile(File file) throws WavFileException {
        return new WavFileInputStream(file);
    }
}
