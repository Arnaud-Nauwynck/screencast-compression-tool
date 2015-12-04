package fr.an.screencast.compressor.dtos.color;

import java.io.Serializable;

/**
 * store the last N Last-Recently-Used colors, and increment counter for each changes
 * 
 * see other implementation for MRU:Most-Recently-Used (!= Last) 
 *
 */
public class ColorLRUChangeStats implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;

    private int countChange;
    
    private int[] prevColors;
    private int[] frameIndexPrevChanges;
    private int[] hitCounts;
    private int startModulo; // optim avoid moving data in LRU array
    
    // ------------------------------------------------------------------------

    public ColorLRUChangeStats(int N) {
        this.prevColors = new int[N];
        this.frameIndexPrevChanges = new int[N];
        this.hitCounts = new int[N];
    }

    // ------------------------------------------------------------------------

    public void addRGB(int rgb, int frameIndex) {
        final int[] prevColor = this.prevColors;
        int i = startModulo;
        for(; i >= 0; i--) {
            if (prevColor[i] == rgb) {
                hitCounts[i]++;
                return; // found
            }
        }
        for(i = prevColor.length-1; i != startModulo; i--) {
            if (prevColor[i] == rgb) {
                hitCounts[i]++;
                return;
            }
        }
        // not found
        countChange++;
        startModulo++;
        if (startModulo == prevColor.length) {
            startModulo = 0;
        }
        prevColor[startModulo] = rgb;
        hitCounts[startModulo] = 1;
        frameIndexPrevChanges[startModulo] = frameIndex;
    }

    public int getCountChange() {
        return countChange;
    }

    public int[] getPrevColors() {
        return prevColors;
    }

    public int[] getFrameIndexPrevChanges() {
        return frameIndexPrevChanges;
    }

    public int getStartModulo() {
        return startModulo;
    }

    public int[] getHitCounts() {
        return hitCounts;
    }
    
}
