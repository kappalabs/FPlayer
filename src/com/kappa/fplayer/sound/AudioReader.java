
package com.kappa.fplayer.sound;

import com.kappa.fplayer.fft.Complex;
import com.kappa.fplayer.fft.Transform;
import com.kappa.fplayer.graphics.Animator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;

/**
 * Reader for audio data files.
 * Reads data into buffer, data is transformed into processable form.
 * Window function is performed on every data buffer for better transformation
 * (difference can be simply seen by using the rectangular window function instead).
 * After transformation, the data are send to Animator, which will show them in window.
 * 
 * <p><a href="http://en.wikipedia.org/wiki/Window_function">On window functions</a>
 * 
 * @author Vojtech Vasek
 */
public class AudioReader extends SoundReader {

    private AudioInputStream    ais;
    private SourceDataLine      sdl;
    private Complex[]           cdata;              
    private byte[]              rawData;
    private final File          audioFile;
    

    /**
     * Prepare new Sound Reader for reading the input audio file.
     * Initialization will prepare window function and buffer for reading the input.
     * 
     * @param animator animator, which will be painting the results
     * @param audioFile audio file, which will be readed
     */
    public AudioReader(Animator animator, File audioFile) {
        super(animator);
        this.audioFile = audioFile;
    }
    
    /**
     * Main thread for reading and processing the audio.
     * Firstly initialize input and output data line, then stop the erasure process
     * that can be still running on Animator's panel. Start to process the sound.
     * At the end, erase the residual state of Animator panel.
     */
    @Override
    public void run() {
        try {
            init();
            
            running = true;
            animator.stopErasure();
            playAudio();
            animator.performErasure();
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(animator, "Choose an audio file.", "File not found", JOptionPane.ERROR_MESSAGE);
            System.err.println(ex);
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException ex) {
            JOptionPane.showMessageDialog(animator, "Choosen audio file can't be readed.", "Can't load a file", JOptionPane.ERROR_MESSAGE);
            System.err.println(ex);
        }
    }

    /**
     * Prepare Audio Input Stream from audio file, which will be able
     * to simply decode and read the audio data.
     * Also prepare Source Data Line, which is the output stream to the speakers.
     * 
     * @throws IOException
     * @throws UnsupportedAudioFileException
     * @throws LineUnavailableException 
     */
    private void init() throws FileNotFoundException, UnsupportedAudioFileException, LineUnavailableException, IOException {
        if (audioFile == null) {
            throw new FileNotFoundException();
        }
        AudioInputStream in = AudioSystem.getAudioInputStream(audioFile);
        AudioFormat sourceAF = in.getFormat();

        // Desired format of the input stream
        AudioFormat targetAF = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                sourceAF.getSampleRate(), ssib, sourceAF.getChannels(),
                sourceAF.getChannels() * (ssib / 8), sourceAF.getSampleRate(), false);
        
        // Decoded stream in desired format
        ais = AudioSystem.getAudioInputStream(targetAF, in);

        cdata = new Complex[bufferLength];
        for (int i=0; i < cdata.length; i++) {
            cdata[i] = new Complex();
        }

        // Output stream preparation
        DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class, ais.getFormat(), AudioSystem.NOT_SPECIFIED);
        if (!AudioSystem.isLineSupported(lineInfo)) {
            System.err.println("AudioReader: "+lineInfo+" not supported!");
            return;
        }

        sdl = (SourceDataLine) AudioSystem.getLine(lineInfo);
        sdl.open(ais.getFormat(), sdl.getBufferSize());
        
        AudioFormat sdlForm = sdl.getFormat();
        ssib = sdlForm.getSampleSizeInBits();
        if (ssib == AudioSystem.NOT_SPECIFIED) {
            ssib = DEFAULT_SSIB;
        }
        channelCount = sdlForm.getChannels();
        if (channelCount == AudioSystem.NOT_SPECIFIED) {
            channelCount = DEFAULT_CHANNEL_COUNT;
        }
        sampleRate = sdlForm.getSampleRate();
        if (sampleRate == AudioSystem.NOT_SPECIFIED) {
            sampleRate = DEFAULT_SAMPLE_RATE;
        }
        frameSize = channelCount * (ssib / 8);
    }

    /**
     * Take initialized object and start reading from them.
     * Raw data are transformed into separate double arrays for every channel.
     * Data from channels are averaged, then window function is performed on them
     * (FFT is designed for cyclic functions, window function tries to adjust input
     * function so that it looks cyclic), after that, we perform FFT and the result
     * is transmitted to Animator. Simultaneously, the data are writed to the
     * speakers.
     * 
     * @throws IOException
     * @throws LineUnavailableException 
     */
    private void playAudio() throws IOException, LineUnavailableException {
        if (sdl == null || ais == null) {
            return;
        }
        
        // Prepare Animator's data
        animator.setAudioInfo(bufferLength, sampleRate);
        // Start reading the input audio
        sdl.start();
        // Wait a moment before reading, reading right after sdl.start() was causing
        // troubles with large audio files.
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            System.err.println(ex);
        }
        
        // Init to 1 just to pass the while condition
        int totalReaded = 1;
        int totalLen = bufferLength * frameSize;
        rawData = new byte[totalLen];
        
        while (running && totalReaded > 0) {
            try {
                int left = totalLen;
                totalReaded = 0;
                int tmp;
                while (left > 0 && (tmp = ais.read(rawData, totalLen - left, left)) != -1) {
                    left -= tmp;
                    totalReaded += tmp;
                }
            } catch (IndexOutOfBoundsException ex) {
                // One of the known MP3SPI1.9.5 bugs and its recommended solution -- try to read again
                totalReaded = 0;
            }
            if (totalReaded > 0) {
                int left = totalReaded;
                while (left > 0) {
                    int tmp = sdl.write(rawData, totalReaded - left, left);
                    left -= tmp;
                }

                double[] averageData = averageChannels(toChannels(rawData));
                for (int i = averageData.length - 1; i < cdata.length; i++) {
                    cdata[i].set(0, 0);
                }
                for (int i=0; i < cdata.length; i++) {
                    cdata[i].setImaginary(0);
                    cdata[i].setReal(averageData[i]);
                }
                Transform.applyWindow(cdata, window);
                animator.setData(Transform.transform(cdata));
                try {
                    animator.updateState();
                } catch (Exception ex) {
                    System.err.println(ex);
                }
            }
        }
        // Stop reading audio and close input channels
        sdl.drain();
        sdl.stop();
        sdl.close();
        ais.close();
    }

    /**
     * From given bytes and audio information, compute domain-based audio values.
     * The input data array consists of frames, which contains values for all channels in the
     * current frame/sample of the audio file.
     * 
     * @param data readed array of bytes
     * @return retrieved audio data as double arrays for every channel
     */
    private double[][] toChannels(byte[] data) {
        int sampleSize = 1 << (ssib - 1);
        // For better performance (multiplication instead of division will be used)
        double sampleSizeDiv = 1.0 / sampleSize;
        int frameCount = data.length / frameSize;
        int channelSize = frameSize / channelCount;
        
        double[][] channels = new double[channelCount][frameCount];

        // For all of the input data
        for (int i = 0, framePos = 0; i < frameCount; i++, framePos += frameSize) {
            // For every channel
            for (int channel = 0, channelPos = 0; channel < channelCount; channel++, channelPos += channelSize) {
                int sampleValue = 0;
                
                for (int bytePos = 0, bit = 0; bit < ssib-8; bytePos++, bit += 8) {
                    sampleValue |= (data[channelPos + framePos + bytePos] & 0xff) << bit;
                }
                sampleValue |= (data[channelPos + framePos + ssib/8 - 1]) << (ssib - 8);
                
                // Normalize into [-1,1] -- divide by maximum number that can be represented
                channels[channel][i] = sampleValue * sampleSizeDiv;
            }
        }

        return channels;
    }
}