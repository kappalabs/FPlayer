package com.kappa.fplayer;

import com.kappa.fplayer.graphics.Animator;
import com.kappa.fplayer.graphics.TagEditor;
import com.kappa.fplayer.sound.AudioReader;
import com.kappa.fplayer.sound.MicrophoneReader;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * This class will initialize the main frame with its components itself.
 * 
 * <a href="http://stefaanlippens.net/javasmp3">MP3 library inspiration</a>
 * <a href="http://www.javazoom.net/mp3spi/sources.html">Used MP3 library (MP3SPI 1.9.5) URL</a>
 * <a href="http://www.jthink.net/jaudiotagger/">Used audio tagging library (JAudioTagger)</a>
 * 
 * <p><b>TODO:</b> Animator's double-buffering
 * 
 * @author      Vojtech Vasek
 * @version     1.0
 */
public class FPlayer {
    
    /**
     * Name of this program.
     */
    public static final String PROGNAME = "FPlayer";
    /**
     * Percentual coverage of the desktop width.
     */
    public static final int    DEFAULT_WIDTH_PART = 80;
    /**
     * Percentual coverage of the desktop height.
     */
    public static final int    DEFAULT_HEIGHT_PART = 70;

    private JFrame jf;
    private Animator anim;
    private AudioReader ar;
    private MicrophoneReader mr;
    private TagEditor te;
    
    private String lastDir = ".";
    private JMenuItem startItem, stopItem, tagItem;
    private File audioFile;
    
    /**
     * Start the main program, show the frame.
     */
    public FPlayer() {
        showFrame();
    }
    
    /**
     * Initialize the main frame and local variables.
     */
    private void showFrame() {
        jf = new JFrame(PROGNAME);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Set the dimension and position of this window
        Toolkit t = Toolkit.getDefaultToolkit();
        Dimension dsize = t.getScreenSize();
        jf.setSize(dsize.width*DEFAULT_WIDTH_PART/100, dsize.height*DEFAULT_HEIGHT_PART/100);
        jf.setLocation((dsize.width - jf.getWidth())/2, (dsize.height - jf.getHeight())/2);
        Container c = jf.getContentPane();
        
        JMenuBar menuBar = new JMenuBar();

        // Menu tab for opening new audio file and similar
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        // For setting the path to audio file, nothing interesting is performed
        JMenuItem openItem = new JMenuItem("Open File");
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        openItem.addActionListener((ActionEvent e) -> {
            JFileChooser fCh = new JFileChooser(lastDir);
            fCh.setAcceptAllFileFilterUsed(false);
            fCh.addChoosableFileFilter(new FileNameExtensionFilter("Audio files", "wav", "mp3", "ogg"));
            fCh.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fCh.setMultiSelectionEnabled(false);
            if (fCh.showOpenDialog(jf) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fCh.getSelectedFile();
                lastDir = selectedFile.getParent();
                audioFile = selectedFile;
            }
            
            if (audioFile != null && audioFile.exists()) {
                terminateFileInput();
                terminateMicrophoneInput();
                startItem.setEnabled(true);
                tagItem.setEnabled(true);
            }
        });
        fileMenu.add(openItem);
        // Read input from a microphone
        JMenuItem microItem = new JMenuItem("Microphone input");
        microItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
        microItem.addActionListener((e) -> {
            terminateFileInput();
            if (mr == null) {
                mr = new MicrophoneReader(anim);
                mr.start();
            }
            stopItem.setEnabled(true);
        });
        fileMenu.add(microItem);
        // Cleanup and exit
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        exitItem.addActionListener((e) -> {
            terminateFileInput();
            terminateMicrophoneInput();
            System.exit(0);
        });
        fileMenu.add(exitItem);
        
        // Menu tab for controling audio and retrieving data
        JMenu audioMenu = new JMenu("Audio");
        menuBar.add(audioMenu);
        // Start playing music
        startItem = new JMenuItem("Play");
        startItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        startItem.addActionListener((e) -> {
            terminateFileInput();
            terminateMicrophoneInput();
            
            ar = new AudioReader(anim, audioFile);
            ar.start();
            
            stopItem.setEnabled(true);
        });
        startItem.setEnabled(false);
        // Stop playing music
        stopItem = new JMenuItem("Stop");
        stopItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
        stopItem.addActionListener((e) -> {
            terminateMicrophoneInput();
            terminateFileInput();
            
            stopItem.setEnabled(false);
        });
        stopItem.setEnabled(false);
        // Open or move to front Tag editor window
        tagItem = new JMenuItem("Tag info");
        tagItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
        tagItem.addActionListener((e) -> {
            if (te == null || te.isVisible() == false) {
                te = new TagEditor(audioFile);
            }
            te.toFront();
            te.repaint();
        });
        tagItem.setEnabled(false);
        audioMenu.add(startItem);
        audioMenu.add(stopItem);
        audioMenu.add(tagItem);
        jf.setJMenuBar(menuBar);
        
        anim = new Animator(jf.getBackground());
        c.add(anim);
        
        jf.setVisible(true);
    }
    
    /**
     * Properly terminate audio file input reading.
     */
    private void terminateFileInput() {
        if (ar != null) {
            ar.terminate();
            try {
                ar.join();
            } catch (InterruptedException ex) {
                System.err.println("FPlayer: AudioReader interrupted. "+ex);
            }
            ar = null;
        }
    }
    
    /**
     * Properly terminate microphone input reading.
     */
    private void terminateMicrophoneInput() {
        if (mr != null) {
            mr.terminate();
            try {
                mr.join();
            } catch (InterruptedException ex) {
                System.err.println("FPlayer: MicrophoneReader interrupted. "+ex);
            }
            mr = null;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FPlayer fpl = new FPlayer();
        });
    }

}
