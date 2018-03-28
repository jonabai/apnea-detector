package com.jonabai.projects.apnea.api.domain;

import com.jonabai.projects.apnea.services.WavFile;

/**
 * A frame buffer work item for processing
 */
public class FrameBufferWorkItem {
    private double[] buffer;
    private boolean inSilence;
    private float silenceInit;
    private WavFile wavFile;
    private int framesRead;
    private long sampleRate;

    public FrameBufferWorkItem(double[] buffer, boolean inSilence, float silenceInit, WavFile wavFile, int framesRead, long sampleRate) {
        this.buffer = buffer;
        this.inSilence = inSilence;
        this.silenceInit = silenceInit;
        this.wavFile = wavFile;
        this.framesRead = framesRead;
        this.sampleRate = sampleRate;
    }

    public double[] getBuffer() {
        return buffer;
    }

    public void setBuffer(double[] buffer) {
        this.buffer = buffer;
    }

    public boolean isInSilence() {
        return inSilence;
    }

    public void setInSilence(boolean inSilence) {
        this.inSilence = inSilence;
    }

    public float getSilenceInit() {
        return silenceInit;
    }

    public void setSilenceInit(float silenceInit) {
        this.silenceInit = silenceInit;
    }

    public WavFile getWavFile() {
        return wavFile;
    }

    public void setWavFile(WavFile wavFile) {
        this.wavFile = wavFile;
    }

    public int getFramesRead() {
        return framesRead;
    }

    public void setFramesRead(int framesRead) {
        this.framesRead = framesRead;
    }

    public long getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(long sampleRate) {
        this.sampleRate = sampleRate;
    }
}
