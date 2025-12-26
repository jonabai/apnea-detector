package com.jonabai.projects.apnea.api.domain;

/**
 * Breathing pause classification types with severity levels.
 */
public enum BreathingPauseType {
    NOT_SET("Unclassified", 0),
    NORMAL("Normal breathing pause", 0),
    HYPOPNEA("Reduced airflow event", 1),
    MILD_APNEA("Mild apnea event (10-20s)", 2),
    MODERATE_APNEA("Moderate apnea event (20-30s)", 3),
    SEVERE_APNEA("Severe apnea event (>30s)", 4);

    private final String description;
    private final int severityLevel;

    BreathingPauseType(String description, int severityLevel) {
        this.description = description;
        this.severityLevel = severityLevel;
    }

    public String getDescription() {
        return description;
    }

    public int getSeverityLevel() {
        return severityLevel;
    }

    /**
     * Check if this type indicates a potential health concern.
     */
    public boolean isHealthConcern() {
        return severityLevel >= 2;
    }

    /**
     * Check if this type indicates any abnormal breathing pattern.
     */
    public boolean isAbnormal() {
        return severityLevel >= 1;
    }
}
