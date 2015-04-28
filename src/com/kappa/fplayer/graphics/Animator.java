
package com.kappa.fplayer.graphics;

import com.kappa.fplayer.fft.Complex;
import com.kappa.fplayer.sound.Band;
import com.kappa.fplayer.sound.SoundReader;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 *
 * Main panel for towers animation.
 * Each tower represents/shows maximum decibel value of frequency, which this tower
 * covers. Frequency interval is for each tower stored in Band object.
 * 
 * @author Vojtech Vasek
 */
public class Animator extends JPanel implements Runnable {
    
    /**
     * Defines number of towers visible.
     */
    public static final int     DEFAULT_OCTAVE_DENUM = 6;
    /**
     * ISO Octave standard, no need to change this.
     * This frequency refers to most sensitive freqency for human year.
     */
    public static final int     DEFAULT_OCTAVE_BASE  = 1000;
    /**
     * Before showing the tower, the dB value is adjusted to better cover the [0,100] interval.
     * Idea is based on fact, that 100 dB is unpleasant for human year, therefore values as high
     * will not be usualy reached in audio files.
     */
    public static final double  DEFAULT_SCALE_FACTOR = 1.7;
    /**
     * How quickly should the towers be moved (raise/fall step).
     */
    public static final double  DEFAULT_MOVEMENT_SPEED = 1.0/8;
    
    private Complex[]   data;
    private Tower[]     towers;
    private Band[]      bands;
    
    private final int octaveDenum;
    private final boolean normalize = false;
    
    private Timer erasureTimer;
    private int bufferLength, sampleRate;
    private BufferedImage buffImage;
    private Graphics2D graphics;
    protected GraphicsConfiguration gc;
    
    /**
     * Initialize default values and prepare towers with their bands.
     */
    public Animator() {
        octaveDenum = DEFAULT_OCTAVE_DENUM;
        bufferLength = SoundReader.DEFAULT_BUFFER_LENGTH;
        sampleRate = SoundReader.DEFAULT_SAMPLE_RATE;
        init();
    }
    
    /**
     * Prepare the towers with bands for them.
     */
    private void init() {
        gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        
        bands = Band.countISOBands(octaveDenum, DEFAULT_OCTAVE_BASE);
        
        towers = new Tower[bands.length];
        for (int i=0; i < towers.length; i++) {
            towers[i] = new Tower(bands[i]);
            towers[i].setLabel(bands[i].toString());
        }
        
        // Timer, which will wait for the towers to fall down
        erasureTimer = new Timer(42, (e) -> {
            if (data == null || towers == null) {
                erasureTimer.stop();
                return;
            }
            for (Complex c : data) {
                c.set(0, 0);
            }
            // Are the towers all cleared out?
            boolean done = true;
            for (Tower t : towers) {
                if (!t.isZero()) {
                    done = false;
                    break;
                }
            }
            if (done) {
                erasureTimer.stop();
            }
            updateState();
        });
    }
    
    /**
     * Set information about audio data that will be processed.
     * This information is necessary to obtain the right index into data array
     * for given frequency after FFT processing.
     * 
     * @param bufferLength maximum length of input data
     * @param sampleRate number of samples in one second
     */
    public void setAudioInfo(int bufferLength, int sampleRate) {
        this.bufferLength = bufferLength;
        this.sampleRate = sampleRate;
        
        bands = Band.countISOBands(octaveDenum, DEFAULT_OCTAVE_BASE, sampleRate);
        towers = new Tower[bands.length];
        for (int i=0; i < towers.length; i++) {
            towers[i] = new Tower(bands[i]);
            towers[i].setLabel(bands[i].toString());
        }
    }

    /**
     * Called by SoundReader requesting to paint new data analysis.
     * 
     * @param data audio data values after FFT was processed on them
     */
    public void setData(Complex[] data) {
        this.data = data;
    }
    
    /**
     * After calling this method, towers will slowly fall to the bottom.
     */
    public void performErasure() {
        erasureTimer.start();
    }
    
    /**
     * This method will stop erasuring the towers, i.e. moving them down.
     */
    public void stopErasure() {
        erasureTimer.stop();
    }
    
    /**
     * Check, if the situation changed, e.g. initialization,
     * window resize etc. and adapt to that situation.
     */
    private void checkGraphics() {
        if (buffImage == null || graphics == null || (buffImage.getWidth() != getWidth() || buffImage.getHeight() != getHeight())) {
            buffImage = gc.createCompatibleImage(getWidth(), getHeight());
            graphics = buffImage.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, buffImage.getWidth(), buffImage.getHeight());
        }
    }
    
    /**
     * Count the animation of all the towers and repaint them.
     * 
     * The number of visible slabs in Tower is set by percentual value ([0,100]),
     * and it is counted from maximum decibel value in the Band of that Tower.
     * The decibel value must be scaled, because 100dB is usualy unpleasant for human,
     * music will not usualy go to this values and we want the values
     * to be distributed in [0,100] interval. Therefore we use SCALE_FACTOR
     * to make bigger difference between the values.
     * 
     * <p>Other option is to use normalization. The negative side of that, is that
     * even a noise will be shown as 'loud'.
     */
    public void updateState() {
        checkGraphics();
        
        double min = 0, max = Double.MIN_VALUE;
        if (normalize) {
            for (Complex c : data) {
                double akt = DEFAULT_SCALE_FACTOR * Complex.decibelv2(c);
                if (akt > max && !Double.isInfinite(akt)) {
                    max = akt;
                }
                if (akt < min && !Double.isInfinite(akt)) {
                    min = akt;
                }
            }
        }
        
        Tower t;
        Band b;
        double bmax;
        // Recompute state of every tower
        for (Tower tower : towers) {
            t = tower;
            b = t.getBand();
            bmax = 0;
            // Find the biggest value in the band which this tower covers
            for (int i = b.frequencyStart; i < b.frequencyEnd; i++) {
                // Index into data[] does not refers to the same frequency directly, transformation must be performed
                double nvalue = DEFAULT_SCALE_FACTOR * Complex.decibelv2(data[(i*bufferLength/sampleRate)]);
                if (normalize) {
                    nvalue = normalize(min, max, nvalue);
                }
                if (nvalue > bmax) {
                    bmax = nvalue;
                }
            }
            // The value, which the tower differs from the state, in which it should be
            double diff = bmax - t.getValue();
            if (diff != 0) {
                // One-step move -- smooth animation, can't react to quick changes
//                t.setValue(t.getValue() + diff/Math.abs(diff));
                // Acceleration move
                t.setValue(t.getValue() + Math.floor(diff*DEFAULT_MOVEMENT_SPEED));
                // Jump move -- immediate change of state to where it should be
//                t.setValue(t.getValue() + diff);
            }
        }
        // Paint the new state of every tower into the image buffer
        int towerLen = getWidth()/towers.length;
        for (int i=0; i < towers.length; i++) {
            towers[i].render(graphics, i*towerLen, 0, towerLen, getHeight());
        }
        EventQueue.invokeLater(this);
    }

    /**
     * Take the prepared buffered image and paint it on this panel.
     * 
     * @param g Graphics object, where this image will be painted
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (buffImage != null) {
            g.drawImage(buffImage, 0, 0, null);
        }
    }
    
    /**
     * Transform given value from the interval [min, max] onto [0, 100].
     * 
     * @param min minimal measured value
     * @param max maximal measured value
     * @param value value to be converted
     * @return transformed value
     */
    private double normalize(double min, double max, double value) {
        return (value-min)/(max-min)*100;
    }
    
    /**
     * Inverse operation to normalize().
     * Transform given value from the interval [0, 100] onto [min, max].
     * 
     * @param min minimal measured value
     * @param max maximal measured value
     * @param value value to be converted
     * @return transformed value
     */
    private double denormalize(double min, double max, double value) {
        return value/100*(max-min) + min;
    }

    /**
     * Perform repainting of this panel.
     */
    @Override
    public void run() {
        repaint();
    }
}
