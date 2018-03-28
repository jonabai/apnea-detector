package com.jonabai.projects.apnea.api.domain;

/**
 * A breathing pause
 */
public class BreathingPause {
    private String filePath;
    private int index;
    private float start;
    private float end;
    private BreathingPauseType type = BreathingPauseType.NOT_SET;

    public BreathingPause(String filePath, int index, float start, float end, BreathingPauseType type) {
        this.filePath = filePath;
        this.index = index;
        this.start = start;
        this.end = end;
        this.type = type;
    }

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

    public void setType(BreathingPauseType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "BreathingPause{" +
                "filePath='" + filePath + '\'' +
                ", index=" + index +
                ", start=" + start +
                ", end=" + end +
                ", type=" + type +
                '}';
    }
}
