package com.jonabai.projects.apnea.api.domain;


/**
 * Wav file exception
 */
public class WavFileException extends Exception {

    public WavFileException(String message)
    {
        super(message);
    }

    public WavFileException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
