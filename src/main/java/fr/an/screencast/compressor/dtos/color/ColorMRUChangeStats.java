package fr.an.screencast.compressor.dtos.color;

import java.io.Serializable;

/**
 * store the last N Most-Recently-Used colors, and increment counter for each changes 
 *
 * see also ColorMRUChangeStatsImg (using image data instead of small object allocated + pointers) 
 */
public class ColorMRUChangeStats implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;

    private int countChange;
    
    private int[] mostUsedColors; // sorted per hitCounts
    private int[] frameIndexPrevChanges;
    private int[] hitCounts;
    
    // ------------------------------------------------------------------------

    public ColorMRUChangeStats(int N) {
        this.mostUsedColors = new int[N];
        this.frameIndexPrevChanges = new int[N];
        this.hitCounts = new int[N];
    }

    // ------------------------------------------------------------------------

    public int findRGB(int rgb) {
        final int[] mostUsedColors = this.mostUsedColors;
        int i = 0;
        for(; i < mostUsedColors.length; i++) {
            if (mostUsedColors[i] == rgb) {
                return i;
            }
        }
        return -1;
    }
    
    public void addRGB(int rgb, int frameIndex) {
        final int len = mostUsedColors.length;
        final int[] mostUsedColors = this.mostUsedColors;
        int i = 0;
        for(; i < len; i++) {
            if (mostUsedColors[i] == rgb) {
                hitCounts[i]++;
                
                if (i != 0 && hitCounts[i] > hitCounts[i-1]) {
                    // re-order (swap i-1 <> i)
                    int tmp;
                    tmp = mostUsedColors[i-1]; mostUsedColors[i-1] = mostUsedColors[i]; mostUsedColors[i] = tmp;
                    tmp = hitCounts[i-1]; hitCounts[i-1] = hitCounts[i]; hitCounts[i] = tmp;
                    tmp = frameIndexPrevChanges[i-1]; frameIndexPrevChanges[i-1] = frameIndexPrevChanges[i]; frameIndexPrevChanges[i] = tmp;
                }
                
                return; // found
            }
            if (hitCounts[i] == 0) {
                break; // unused slot
            }
        }
        // not found
        countChange++;
        if (i == mostUsedColors.length) {
            i--;
        }
        int last = i;
        // insert above "Last" recently used having same rank 1 ... shift right remaining
        while(i-1 >= 0 && hitCounts[i-1]==1) {
            i--;
        }
        // insert or overwrite at [i]
        int shiftLen = last - i;
        System.arraycopy(mostUsedColors, i, mostUsedColors, i+1, shiftLen);
        System.arraycopy(hitCounts, i, hitCounts, i+1, shiftLen);
        System.arraycopy(frameIndexPrevChanges, i, frameIndexPrevChanges, i+1, shiftLen);

        mostUsedColors[i] = rgb;
        hitCounts[i] = 1;
        frameIndexPrevChanges[i] = frameIndex;
    }

    public int getCountChange() {
        return countChange;
    }

    public int[] getMostUsedColors() {
        return mostUsedColors;
    }

    public int[] getFrameIndexPrevChanges() {
        return frameIndexPrevChanges;
    }

    public int[] getHitCounts() {
        return hitCounts;
    }
    
}
