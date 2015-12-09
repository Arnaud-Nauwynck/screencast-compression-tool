package fr.an.screencast.compressor.imgtool.delta;

import java.io.Serializable;

import fr.an.screencast.compressor.imgtool.utils.FastModuloUtils;

/**
 * store the last N Last-Recently-Used values, and increment counter for each changes
 * 
 * see other implementation for MRU:Most-Recently-Used (!= Last) 
 *
 */
public class IntValueLRUChangeHistory implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;

    // private final int historyLen : computed field = prevValues.length;
    private int countChange;
    private int startModulo; // optim avoid moving data in LRU array
    
    private int[] prevValues;
    private int[] prevFrameIndexes;
    
    // ------------------------------------------------------------------------

    public IntValueLRUChangeHistory(int N) {
        this.prevValues = new int[N];
        this.prevFrameIndexes = new int[N];
    }

    // ------------------------------------------------------------------------

    public void addTimeValue(int frameIndex, int value) {
        int prevValue = prevValues[startModulo];
        if (prevValue != value) {
            countChange++;
            startModulo++;
            if (startModulo == prevValues.length) {
                startModulo = 0;
            }
            prevValues[startModulo] = value;
            prevFrameIndexes[startModulo] = frameIndex;
        }
    }

    public int getCountChange() {
        return countChange;
    }

    public int getNthPrevValue(int n) {
        if (0 > n || n >= prevValues.length) {
            throw new IllegalArgumentException();
        }
        return prevValues[_nthPrevInternalModuloIndex(n)];
    }

    public int getNthPrevFrameIndex(int n) {
        if (0 > n || n >= prevValues.length) {
            throw new IllegalArgumentException();
        }
        return prevFrameIndexes[_nthPrevInternalModuloIndex(n)];
    }

    public int _nthPrevInternalModuloIndex(int n) {
        return FastModuloUtils.minusModulo(startModulo, n, prevValues.length);
    }

    public int _getPrevValue(int i) {
        return prevValues[i];
    }

    public int _getPrevFrameIndex(int i) {
        return prevFrameIndexes[i];
    }

    
}
