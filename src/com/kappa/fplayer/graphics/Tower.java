
package com.kappa.fplayer.graphics;

import com.kappa.fplayer.sound.Band;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * Class for handeling a tower object, which is the base component of the
 * main program's visualization panel.
 * The tower consists of slabs, which are small parts. Number of visible slabs
 * represents the current value this tower stores.
 * 
 * @author Vojtech Vasek
 */
public class Tower {
    
    /**
     * How the tower should look like.
     * Slabs style will make the tower made from small parts.
     * Filled means, that the tower will be one object with more precise movements.
     */
    public enum Style {
        slabs, filled;
    }
    
    public static final int DEFAULT_SLABS_NUMBER    = 42;
    public static final int DEFAULT_X_PADDING       = 2;
    public static final int DEFAULT_Y_PADDING       = 2;
    public static final int DEFAULT_CAP_DELAY       = 12;
    public static final Style DEFAULT_TOWER_STYLE   = Style.slabs;
    
    private final int   slabsNum;
    private final int   xpadding, ypadding;
    private final Band  band;
    private final Style towerStyle;
    
    private int     capSlabPosition;    // Position of the recent max showed value
    private int     capSlabWaitTime;    // How much time the cap should wait until it can fall down
    
    private double  value;      // Value from interval [0,100], percentage of visible slabs
    private String  label;      // Label showed on the bottom of this tower
    FontMetrics     labelFM;    // Font style for the 'label' text

    
    /**
     * Create tower with default values, assign given frequency band to it.
     * 
     * @param band frequency band to be assigned for this tower
     */
    public Tower(Band band) {
        this.band = band;
        this.slabsNum = DEFAULT_SLABS_NUMBER;
        this.xpadding = DEFAULT_X_PADDING;
        this.ypadding = DEFAULT_Y_PADDING;
        this.towerStyle = DEFAULT_TOWER_STYLE;
    }
    
    /**
     * Create new tower object with more precise information.
     * This can be useful if every tower should look different.
     * 
     * @param band frequency band to be assigned for this tower
     * @param label label for the bottom part of the tower
     * @param slabsNum maximum number of slabs in this tower
     * @param xpadding padding from the left and right side of the painted area
     * @param ypadding padding from the top and bottom side of the painted area
     */
    public Tower(Band band, String label, int slabsNum, int xpadding, int ypadding) {
        this.band = band;
        this.label = label;
        this.slabsNum = slabsNum;
        this.xpadding = xpadding;
        this.ypadding = ypadding;
        this.towerStyle = DEFAULT_TOWER_STYLE;
    }
    
    /**
     * Paint this tower onto Graphics2D object, take 'x' and 'y' as an origin,
     * and 'width' and 'height' as a space, where the tower can be painted.
     * 
     * @param g where this tower should be painted
     * @param backgroundColor desired color of the background
     * @param x x origin
     * @param y y origin
     * @param width width of the available space
     * @param height height of the available space
     */
    //ALF: Preferably, double buffering should be used
    public void render(Graphics2D g, Color backgroundColor, int x, int y, int width, int height) {
        // Erasure of previously painted Tower
        g.setColor(backgroundColor);
        g.fillRect(x, y, width, height);
        
        // Height of one tower slab can be counted from total number of 'slabsNum' desired
        double slabHeight = (height - 2*slabsNum*ypadding) / slabsNum;
        int slabWidth = width - 2*xpadding;
        double slabHDiff = slabHeight + 2*ypadding;
        // Defines the position on a hue axe of the HSB color cilinder
        float cilinderPos = 0f;
        // We want the 'cilinderPos' value to be in [0; 1] interval, number of steps is at most 'slabsNum'
        float magStep = (float)(1 - cilinderPos) / slabsNum;
        // Draw all slabs in tower which should be visible (depends on 'value')
        g.setColor(Color.GREEN);
        switch (towerStyle) {
            case filled: 
                g.fillRect(x + xpadding, height - (int)(value/100.0*height) + y + ypadding, slabWidth, height - 2*ypadding);
                break;
            case slabs:
                for (int i=1; i <= getVisibleSlabs(); i++) {
                    cilinderPos += magStep;
                    // Hue will define color, 120deg is green, 0deg is red
                    g.setColor(Color.getHSBColor((120.0F - cilinderPos*120.0F) / 360.0F, 1.0F, 1.0F));

                    g.fillRoundRect(x + xpadding, y + height - (int)(i*slabHDiff), slabWidth, (int)slabHeight, 3, 3);

                    // Is it time to raise the recent top value (the cap)?
                    if (i >= capSlabPosition) {
                        capSlabPosition = i;
                        capSlabWaitTime = DEFAULT_CAP_DELAY;
                    }
                }
                break;
        }
        // Draw the cap on top
        if (capSlabWaitTime == 0 && capSlabPosition > 0) {
            capSlabPosition--;
        } else if (capSlabWaitTime > 0) {
            capSlabWaitTime--;
        }
        g.setColor(Color.getHSBColor((120.0F - 1.0f/slabsNum*(capSlabPosition + 12)*120.0F) / 360.0F, 1.0F, 1.0F));
        g.fill3DRect(x + xpadding, y + height - (int)(capSlabPosition * slabHDiff), slabWidth, (int)slabHeight, true);
        
        // Draw a label on the bottom of this tower
        if (labelFM == null) {
            Font f = new Font("arial", Font.PLAIN, 8);
            g.setFont(f);
            labelFM = g.getFontMetrics();
        }
        g.setColor(Color.BLACK);
        g.setFont(labelFM.getFont());
        g.drawString(toString(), x + xpadding + (slabWidth - labelFM.stringWidth(toString()))/2, y + height - ypadding - (int)slabHeight/2);
    }

    /**
     * Set a label to this tower.
     * 
     * @param label String to be set as a label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Return String label of this tower.
     * 
     * @return String label of this tower
     */
    public String getLabel() {
        return label;
    }

    /**
     * Return how many slabs this tower consists of.
     * 
     * @return number of slabs this tower consists of
     */
    public int getSlabsNum() {
        return slabsNum;
    }
    
    /**
     * Returns label which was set, or 'value' as a String if label is null.
     * 
     * @return label if not null, dB value otherwise
     */
    @Override
    public String toString() {
        if (label == null) {
            return String.format("%1$,.0f", value);
        }
        return label;
    }
    
    /**
     * Return how many slabs are visible in this tower.
     * 
     * @return how many slabs are visible in this tower
     */
    public int getVisibleSlabs() {
        return getVisibleSlabs(slabsNum, value);
    }
    
    /**
     * For given number of maximum number of slabs and percentage value,
     * counts how many slabs that value represents.
     * 
     * @param slabsNum maximum number of slabs in tower
     * @param value percentage value
     * @return how many slabs would be visible
     */
    public static int getVisibleSlabs(int slabsNum, double value) {
        return Math.min((int)Math.ceil(slabsNum * value / 100.0), slabsNum);
    }
    
    /**
     * From number of slabs given, return percentage value of how much
     * visible slabs would that represent.
     * 
     * @param vslabs number of visible slabs
     * @return percentage representation of that number
     */
    public double slabsToPercentage(int vslabs) {
        return vslabs * 100 / slabsNum;
    }
    
    /**
     * Set how many slabs in this tower are visible.
     * 
     * @param vslabs how many slabs in this tower should be visible
     */
    public void setVisibleSlabs(int vslabs) {
        value = vslabs * 100 / slabsNum;
    }

    /**
     * Return current 'value', i.e. percentage of showed slabs of the tower.
     * 
     * @return current 'value' (from interval [0,100])
     */
    public double getValue() {
        return value;
    }

    /**
     * Set internal 'value', to given value.
     * If the value is not in allowed bounds, it's changed to fulfill them.
     * 
     * @param value new desired value
     */
    public void setValue(double value) {
        if (value < 0) {
            this.value = 0;
        } else if (value > 100) {
            this.value = 100;
        } else {
            this.value = value;
        }
    }

    /**
     * Return true, if this tower is at zero position, i.e. it's not
     * visible, i.e. all parts falled down.
     * 
     * @return true if tower is active
     */
    public boolean isZero() {
        return capSlabPosition <= 0;
    }

    /**
     * Return band associated with this tower.
     * 
     * @return band associated with this tower
     */
    public Band getBand() {
        return band;
    }
    
}
