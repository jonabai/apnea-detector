package com.jonabai.projects.apnea.api.domain;

/**
 * An immutable breathing pause record.
 */
public record BreathingPause(
        String filePath,
        int index,
        float start,
        float end,
        BreathingPauseType type
) {
    /**
     * Compact constructor with validation.
     */
    public BreathingPause {
        if (start < 0 || end < 0) {
            throw new IllegalArgumentException("Start and end times must be non-negative");
        }
        if (end < start) {
            throw new IllegalArgumentException("End time must be >= start time");
        }
        if (type == null) {
            type = BreathingPauseType.NOT_SET;
        }
    }

    /**
     * Factory method to create an unclassified breathing pause.
     */
    public static BreathingPause unclassified(String filePath, int index, float start, float end) {
        return new BreathingPause(filePath, index, start, end, BreathingPauseType.NOT_SET);
    }

    /**
     * Returns a new BreathingPause with the specified type.
     */
    public BreathingPause withType(BreathingPauseType newType) {
        return new BreathingPause(filePath, index, start, end, newType);
    }

    /**
     * Calculate the duration of the pause in seconds.
     */
    public float duration() {
        return end - start;
    }

    // Backward compatibility getters for existing code
    public String getFilePath() {
        return filePath;
    }

    public int getIndex() {
        return index;
    }

    public float getStart() {
        return start;
    }

    public float getEnd() {
        return end;
    }

    public BreathingPauseType getType() {
        return type;
    }
}
