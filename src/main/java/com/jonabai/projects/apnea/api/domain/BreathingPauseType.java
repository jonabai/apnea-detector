package com.jonabai.projects.apnea.api.domain;

/**
 * Breathing pause classification types.
 */
public enum BreathingPauseType {
    NOT_SET("Unclassified"),
    NORMAL("Normal breathing pause"),
    APNEA("Sleep apnea event detected");

    private final String description;

    BreathingPauseType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if this type indicates a potential health concern.
     */
    public boolean isHealthConcern() {
        return this == APNEA;
    }
}
