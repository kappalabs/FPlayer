package com.kappa.fplayer.sound;

import com.kappa.fplayer.fft.Transform;
import com.kappa.fplayer.graphics.Animator;

/**
 * General structure of every sound reader, i.e. file, microphone readers...
 * 
 * @author Vojtech Vasek
 */
public abstract class SoundReader extends Thread {
    
    /**
     * Type of window function -- adjustment performed on audio data sample to
     * achieve better/cleaner time domain to frequency domain transformation.
     */
    public static enum WindowType {
        rectangular, hamming, hanning;
    }
        
    /**
     * Must be power of two, 4k is generaly recomended value.
     */
    public static final int         DEFAULT_BUFFER_LENGTH   = 4096/4;
    public static final int         DEFAULT_SAMPLE_RATE     = 44100;
    public static final int         DEFAULT_SSIB            = 16;
    public static final int         DEFAULT_CHANNEL_COUNT   = 2;
    public static final WindowType  DEFAULT_WINDOW_TYPE     = WindowType.hamming;

    protected WindowType windowType;
    protected double[]   window;
    protected int        bufferLength;
    protected float      sampleRate;
    protected int        ssib;
    protected int        channelCount;
    protected int        frameSize;
    
    Animator animator;
    protected volatile boolean running;

    public SoundReader(Animator animator) {
        this.animator = animator;
        windowType = DEFAULT_WINDOW_TYPE;
        bufferLength = DEFAULT_BUFFER_LENGTH;
        sampleRate = DEFAULT_SAMPLE_RATE;
        ssib = DEFAULT_SSIB;
        frameSize = channelCount * (ssib / 8);
        switch (windowType) {
            case hanning:
                window = Transform.hanningWindow(bufferLength);
                break;
            case hamming:
                window = Transform.hammingWindow(bufferLength, 0.53836, 0.46164);
                break;
            default:
                // No window usage (rectangular window has neutral effect)
                window = Transform.rectangularWindow(bufferLength);
        }
        
        if (animator != null) {
            animator.setAudioInfo(bufferLength, sampleRate);
        }
    }
    
    /**
     * For given multiple channels, return one, that is an average from
     * all of them.
     * 
     * @param channels channels to be averaged
     * @return one channel that is an average of the input ones
     */
    protected double[] averageChannels(double[][] channels) {
        if (channels == null || channels.length == 0) {
            return null;
        }
        
        // For better performance
        double lenDiv = 1.0 / channels.length;
        
        double[] ret = new double[channels[0].length];
        double average;
        for (int i=0; i < channels[0].length; i++) {
            average = 0;
            for (double[] channel : channels) {
                average += channel[i];
            }
            ret[i] = average * lenDiv;
        }
        
        return ret;
    }
    
    /**
     * Terminate this thread, i.e. reading/playing audio.
     */
    public void terminate() {
        running = false;
    }
    
}
