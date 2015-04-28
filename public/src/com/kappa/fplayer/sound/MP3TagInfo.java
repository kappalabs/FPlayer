
package com.kappa.fplayer.sound;

import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.ID3v1Tag;

/**
 * MP3 tags processor.
 * Tries to retrieve more information from ID3v1 tags format.
 * 
 * @author Vojtech Vasek
 */
public class MP3TagInfo extends TagInfo {
    
    /**
     * Try to get some extra information on top of the basic ones.
     * 
     * @param audioFile MP3 file for getting the information from
     */
    @Override
    public void loadInfo(File audioFile) {
        try {
            MP3File mpf = (MP3File)AudioFileIO.read(audioFile);
            MP3AudioHeader ah = mpf.getMP3AudioHeader();
            
            loadBasicInfo(audioFile);
            headerInfo.add(new TagElement("emphasis", ah.getEmphasis()));
            headerInfo.add(new TagElement("encoder", ah.getEncoder()));
            
            ID3v1Tag tag  = mpf.getID3v1Tag();
            if (tag == null) {
                return;
            }
            audioInfo.add(new TagElement("genre", tag.getFirstGenre()));
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException ex) {
            JOptionPane.showMessageDialog(null, "Can't retrieve MP3 tags.", "Tag ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

}
