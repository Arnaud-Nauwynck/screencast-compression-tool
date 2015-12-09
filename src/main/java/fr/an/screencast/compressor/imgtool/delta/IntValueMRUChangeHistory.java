package fr.an.screencast.compressor.imgtool.delta;

import java.io.Serializable;

/**
 * store the last N Most-Recently-Used valuees, and increment counter for each changes 
 *
 * see also IntImageMRUChangeHistory (using image data instead of small object allocated + pointers) 
 */
public class IntValueMRUChangeHistory implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;

    private int countChange;
    
    private int[] mostUsedValues; // sorted per hitCounts
    private int[] frameIndexPrevChanges;
    private int[] hitCounts;
    
    // ------------------------------------------------------------------------

    public IntValueMRUChangeHistory(int N) {
        this.mostUsedValues = new int[N];
        this.frameIndexPrevChanges = new int[N];
        this.hitCounts = new int[N];
    }

    // ------------------------------------------------------------------------

    public int findValue(int value) {
        final int[] mostUsedValues = this.mostUsedValues;
        int i = 0;
        for(; i < mostUsedValues.length; i++) {
            if (mostUsedValues[i] == value) {
                return i;
            }
        }
        return -1;
    }
    
    public void addValue(int value, int frameIndex) {
        final int len = mostUsedValues.length;
        final int[] mostUsedValues = this.mostUsedValues;
        int i = 0;
        for(; i < len; i++) {
            if (mostUsedValues[i] == value) {
                hitCounts[i]++;
                
                if (i != 0 && hitCounts[i] > hitCounts[i-1]) {
                    // re-order (swap i-1 <> i)
                    int tmp;
                    tmp = mostUsedValues[i-1]; mostUsedValues[i-1] = mostUsedValues[i]; mostUsedValues[i] = tmp;
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
        if (i == mostUsedValues.length) {
            i--;
        }
        int last = i;
        // insert above "Last" recently used having same rank 1 ... shift right remaining
        while(i-1 >= 0 && hitCounts[i-1]==1) {
            i--;
        }
        // insert or overwrite at [i]
        int shiftLen = last - i;
        System.arraycopy(mostUsedValues, i, mostUsedValues, i+1, shiftLen);
        System.arraycopy(hitCounts, i, hitCounts, i+1, shiftLen);
        System.arraycopy(frameIndexPrevChanges, i, frameIndexPrevChanges, i+1, shiftLen);

        mostUsedValues[i] = value;
        hitCounts[i] = 1;
        frameIndexPrevChanges[i] = frameIndex;
    }

    public int getCountChange() {
        return countChange;
    }

    public int getMostUsedValues(int i) {
        return mostUsedValues[i];
    }

    public int getFrameIndexPrevChanges(int i) {
        return frameIndexPrevChanges[i];
    }

    public int getHitCounts(int i) {
        return hitCounts[i];
    }
    
}
