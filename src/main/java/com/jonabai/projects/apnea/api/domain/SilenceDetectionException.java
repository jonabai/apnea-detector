package com.jonabai.projects.apnea.api.domain;

/**
 * A silence detection process exception
 */
public class SilenceDetectionException extends RuntimeException {

    public SilenceDetectionException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
