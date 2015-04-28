
package com.kappa.fplayer.sound;

import com.kappa.fplayer.fft.Complex;
import com.kappa.fplayer.fft.Transform;
import com.kappa.fplayer.graphics.Animator;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JOptionPane;

/**
 * Read audio stream from default microphone and show its spectral analysis.
 * 
 * @author Vojtech Vasek
 */
public class MicrophoneReader extends SoundReader {

    /**
     * Prepare new Sound Reader for reading the microphone input.
     * Initialization will prepare window function.
     * 
     * @param animator Animator object, that will serve to show the analysis
     */
    public MicrophoneReader(Animator animator) {
        this.animator = animator;
        
        windowType = DEFAULT_WINDOW_TYPE;
        bufferLength = DEFAULT_BUFFER_LENGTH;
        switch (windowType) {
            case hanning:
                window = Transform.hanningWindow(bufferLength);
                break;
            case hamming:
                window = Transform.hammingWindow(bufferLength, 0.53836, 0.46164);
                break;
            default:
                window = Transform.rectangularWindow(bufferLength);
        }
    }
    
    /**
     * Start reading and processing the input stream from microphone.
     */
    @Override
    public void run() {
        AudioFormat targetAF = new AudioFormat(44100, 8, 1, true, true);

        TargetDataLine line;
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, targetAF);
        if (!AudioSystem.isLineSupported(info)) {
            JOptionPane.showMessageDialog(null, "Cannot obtain data line.", "Microphone Reader ERROR", JOptionPane.ERROR_MESSAGE);
            System.err.println("MicrophoneReader: line not supported!");
        }
        try {
            // Obtain and open the input line
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(targetAF);

            // Start reading the input
            line.start();
        
            int numBytesRead;
            byte[] data = new byte[bufferLength];

            Complex[] cdata = new Complex[bufferLength];
            for (int i=0; i < cdata.length; i++) {
                cdata[i] = new Complex();
            }

            animator.stopErasure();
            running = true;
            while (running) {
                // Read the next chunk of data
                numBytesRead = line.read(data, 0, bufferLength);
                // Can't read more data
                if (numBytesRead < 0) {
                    break;
                }

                for (int i = numBytesRead - 1; i < bufferLength; i++) {
                    cdata[i].set(0, 0);
                }
                for (int i=0; i < bufferLength; i++) {
                    cdata[i].setImaginary(0);
                    cdata[i].setReal(data[i]);
                }
                Transform.applyWindow(cdata, window);
                animator.setData(Transform.transform(cdata));
                animator.updateState();
            }
            animator.performErasure();
        } catch (LineUnavailableException ex) {
            JOptionPane.showMessageDialog(null, "Data line is unavailable.", "Microphone Reader ERROR", JOptionPane.ERROR_MESSAGE);
            System.err.println("MicrophoneReader: line unavailable: "+ex);
        }
    }
}
