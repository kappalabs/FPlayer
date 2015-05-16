
package com.kappa.fplayer.graphics;

import com.kappa.fplayer.sound.GeneralTagInfo;
import com.kappa.fplayer.sound.MP3TagInfo;
import com.kappa.fplayer.sound.TagInfo;
import com.kappa.fplayer.sound.TagInfo.TagElement;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * Small window, that contains two main panels.
 * First one contains information from the header of the audio file.
 * Second one has information about the audio track itself -- tags.
 * 
 * @author Vojtech Vasek
 */
public class TagEditor extends JFrame {

    private final File audioFile;
    private TagInfo ti;

    /**
     * Initialize and show the Tag Editor's window if audio file has some
     * information to be shown and the format is supported.
     * Show error message otherwise.
     * 
     * @param audioFile audio file for analysis
     */
    public TagEditor(File audioFile) {
        this.audioFile = audioFile;
        
        if (audioFile == null || !audioFile.exists()) {
            JOptionPane.showMessageDialog(null, "File does not exists.", "Can't load info", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        init();
    }
    
    /**
     * Analyze the audio file and show the components.
     */
    private void init() {
        if (audioFile.toString().endsWith(".mp3")) {
            ti = new MP3TagInfo();
        } else {
            ti = new GeneralTagInfo();
        }
        
        ti.loadInfo(audioFile);
        // Nothing to be shown
        if (ti.getAudioInfo().isEmpty() && ti.getHeaderInfo().isEmpty()) {
            return;
        }
        
        initGraphics();
    }
    
    /**
     * From loaded information, retrieve and prepare graphical structures.
     */
    private void initGraphics() {
        this.setTitle(audioFile.getName());
        
        JPanel mainPanel = new JPanel(new GridLayout(2, 1));
        JPanel headerInfoPanel = new JPanel(new GridLayout(ti.getHeaderInfo().size(), 2));
        JPanel audioInfoPanel = new JPanel(new GridLayout(ti.getAudioInfo().size(), 2));
        
        TitledBorder titledBorder;
        titledBorder = BorderFactory.createTitledBorder("Header information");
        headerInfoPanel.setBorder(titledBorder);
        for (TagElement te : ti.getHeaderInfo()) {
            JLabel nameL, valueL;
            nameL = new JLabel(te.name+":");
            valueL = new JLabel(te.value.isEmpty() ? "unknown" : te.value);
            headerInfoPanel.add(nameL);
            headerInfoPanel.add(valueL);
        }
        mainPanel.add(headerInfoPanel);
        
        titledBorder = BorderFactory.createTitledBorder("Tags information");
        audioInfoPanel.setBorder(titledBorder);
        for (TagElement te : ti.getAudioInfo()) {
            JLabel nameL, valueL;
            nameL = new JLabel(te.name+":");
            valueL = new JLabel(te.value.isEmpty() ? "unknown" : te.value);
            audioInfoPanel.add(nameL);
            audioInfoPanel.add(valueL);
        };
        mainPanel.add(audioInfoPanel);
        
        add(mainPanel);
        pack();
        Toolkit t = Toolkit.getDefaultToolkit();
        Dimension dsize = t.getScreenSize();
        setLocation((dsize.width - this.getWidth())/2, (dsize.height - this.getHeight())/2);
        setVisible(true);
    }
    
}
