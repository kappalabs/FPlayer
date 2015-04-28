
package com.kappa.fplayer.sound;

import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

/**
 * Loads basic general audio file tags and information.
 * 
 * @author Vojtech Vasek
 */
public class GeneralTagInfo extends TagInfo {

    /**
     * Load simple basic information about the audio file.
     * 
     * @param audioFile file for getting the information from
     */
    @Override
    public void loadInfo(File audioFile) {
        try {
            loadBasicInfo(audioFile);
        } catch (IOException | CannotReadException | TagException | ReadOnlyFileException | InvalidAudioFrameException ex) {
            JOptionPane.showMessageDialog(null, "Tags for this type of file cannot be retrieved.", "Tag ERROR", JOptionPane.ERROR_MESSAGE);
            System.err.println("GeneralTagInfo: "+ex);
        }
    }

}
