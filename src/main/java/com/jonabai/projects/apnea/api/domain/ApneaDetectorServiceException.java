package com.jonabai.projects.apnea.api.domain;

/**
 * Apnea detector service exception
 */
public class ApneaDetectorServiceException extends RuntimeException {

    public ApneaDetectorServiceException(String message)
    {
        super(message);
    }

    public ApneaDetectorServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
