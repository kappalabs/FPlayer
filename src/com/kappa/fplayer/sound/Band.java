
package com.kappa.fplayer.sound;

import java.text.DecimalFormat;
import java.util.ArrayList;


/**
 * Represents one frequency band, which is interval of frequencies with
 * length dependent on frequencies within.
 * Label is set to display the average value of frequencies in band.
 * 
 * @author Vojtech Vasek
 */
public class Band {
    
    public static final int DEFAULT_LOWER_BOUND = 20;
    public static final int DEFAULT_UPPER_BOUND = 22000;
    
    public final int    length;
    public final int    frequencyStart;
    public final int    frequencyEnd;
    public final String label;
    
    
    /**
     * Create band with given frequency interval bounds.
     * 
     * @param frequencyStart the first frequency this band covers
     * @param frequencyEnd the last frequency this band covers
     */
    public Band(int frequencyStart, int frequencyEnd) {
        this.frequencyStart = frequencyStart;
        this.frequencyEnd = frequencyEnd;
        this.length = Math.abs(frequencyEnd - frequencyStart);
        
        DecimalFormat df = new DecimalFormat("###.#");
        double mean = (frequencyEnd + frequencyStart)/2;
        if (mean >= 1000) {
            label = df.format(mean / 1000.0f) + "k";
        } else {
            label = df.format(mean);
        }
    }
    
    /**
     * Wrapper for default sampleRate value (usualy 44100).
     * 
     * @param octaveDenum denominator of the ISO Octave fraction determines how many bands will be present
     * @param baseFreq frequency from where the bands are counted
     * @return array of prepared frequency bands
     */
    public static Band[] countISOBands(int octaveDenum, int baseFreq) {
        return countISOBands(octaveDenum, baseFreq, SoundReader.DEFAULT_SAMPLE_RATE);
    }
    
    /**
     * Counts ISO Octave Bands from given fraction and base frequency, which
     * should be 1000Hz.
     * One band represents interval of frequencies. First few intervals covers only
     * small number of frequencies, later on, the intervals are getting bigger. This relates
     * to the fact, that human ear is more sensitive to lower frequencies (mostly about 1kHz).
     * <p>More on this in the section below.
     * <p>Based on Nyquist theorem, the sample rate must be at least twice the highest frequency,
     * in the audio. Therefore we show only frequencies before sampleRate/2.
     * 
     * <p><a href="http://www.zytrax.com/tech/audio/equalization.html">zytrax.com</a>
     * <p><a href="http://blog.prosig.com/2006/02/17/standard-octave-bands/">prosig.com</a>
     * 
     * @param octaveDenum denominator of the ISO Octave fraction determines how many bands will be present
     * @param baseFreq frequency from where the bands are counted
     * @param sampleRate number of samples per second
     * @return array of prepared frequency bands
     */
    public static Band[] countISOBands(int octaveDenum, int baseFreq, float sampleRate) {
        ArrayList<Band> alb = new ArrayList<>();
        double freqCenter = baseFreq;
        int upper, lower = baseFreq;
        double step10 = Math.pow(10, 3.0 / (10.0 * octaveDenum));
        double step20 = Math.pow(10, 3.0 / (20.0 * octaveDenum));
        
        // Frequencies before base frequency (1kHz)
        while (lower > DEFAULT_LOWER_BOUND) {
            freqCenter /=  step10;
            lower = (int)(freqCenter / step20);
            upper = (int)(freqCenter * step20);
            alb.add(new Band(lower, upper));
        }
        // Frequencies after base frequency (1kHz)
        freqCenter = baseFreq;
        upper = baseFreq;
        while (upper < sampleRate/2.0) {
            freqCenter *=  step10;
            lower = (int)(freqCenter / step20);
            upper = (int)(freqCenter * step20);
            alb.add(new Band(lower, upper));
        }
        alb.sort((Band o1, Band o2) -> ((Integer)o1.frequencyStart).compareTo(o2.frequencyStart));
        
        return alb.toArray(new Band[alb.size()]);
    }
    
    /**
     * Returns label of this band, which contains mean frequency (in Hz).
     * 
     * @return label of this band
     */
    @Override
    public String toString() {
        return label;
    }
}