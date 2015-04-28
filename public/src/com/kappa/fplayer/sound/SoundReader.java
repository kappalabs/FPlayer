package com.kappa.fplayer.sound;

import com.kappa.fplayer.graphics.Animator;
import java.io.File;

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
    public static final int         DEFAULT_BUFFER_LENGTH   = 4096;
    public static final int         DEFAULT_SAMPLE_RATE     = 44100;
    public static final WindowType  DEFAULT_WINDOW_TYPE     = WindowType.hanning;
    
    public WindowType windowType;
    public double[]   window;
    public int        bufferLength;
    
    Animator animator;
    public boolean running;
    public File audioFile;
    
    
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
        
        double[] ret = new double[channels[0].length];
        double average;
        for (int i=0; i < channels[0].length; i++) {
            average = 0;
            for (double[] channel : channels) {
                average += channel[i];
            }
            ret[i] = average / channels.length;
        }
        
        return ret;
    }
    
    /**
     * Terminate this thread, i.e. reading/playing audio.
     */
    public void terminate() {
        running = false;
    }

    /**
     * Set a new Animator, which this Sound Reader will be sending data to.
     * 
     * @param animator Animator instance
     */
    public void setAnimator(Animator animator) {
        this.animator = animator;
    }

    /**
     * Set a new audio file, which this Sound Reader will retrieve data from.
     * 
     * @param audioFile audio file
     */
    public void setAudioFile(File audioFile) {
        this.audioFile = audioFile;
    }
    
}
