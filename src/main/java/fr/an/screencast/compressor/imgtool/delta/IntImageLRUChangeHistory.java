package fr.an.screencast.compressor.imgtool.delta;

import fr.an.screencast.compressor.imgtool.utils.FastModuloUtils;
import fr.an.screencast.compressor.utils.Dim;

/**
 * data structure to keep N-th last changes of color per pixel
 * 
 * see also SlidingImageArray which stores the N-last images 
 * see also IntValueLRUChangeHistory which stores only for 1 value (pixel)
 * see also IntValueMRUChangeHistory which stores the N-most values
 */
public final class IntImageLRUChangeHistory {

    private final Dim dim;
    private final int historyLen;
    
    private int histSlotSize;
    
    /**
     * given indexes x,y,histSlot    (with idxXY=y*width+x,  histSlotSize=1+historyLen*2)
     * => histIndex= idxXY * histSlotSize
     *             = (y*width+x) * (1+historyLen*2)
     * data in range histIndex.. histIndex+histSlotSize
     *    histIndex+0 : startModulo
     *    histIndex+1 : countChange
     *    ..
     *    histIndex+2+i*2+0 : prevValue for [i % historyLen] 
     *    histIndex+2+i*2+1 : prevFrameIndex for [i % historyLen]
     *         
     */
    private int[] data;
    
    private static final int BASE_OFFSET_startModulo = 0;
    private static final int BASE_OFFSET_countChange = 1;
    private static final int OFFSET_prevValue = 0;
    private static final int OFFSET_prevFrameIndex = 1;
    
    // ------------------------------------------------------------------------
    
    public IntImageLRUChangeHistory(Dim dim, int historyLen) {
        this.dim = dim;
        this.historyLen = historyLen;
        this.histSlotSize = 2 + 2*historyLen; 
        this.data = new int[dim.width * dim.height * histSlotSize];
    }

    // ------------------------------------------------------------------------

    public void addImg(int frameIndex, int[] data) {
        final int height = dim.height, width = dim.width;
        for(int y = 0, idx=0; y < height; y++) {
            for(int x = 0; x < width; x++, idx++) {
                addTimeValue(frameIndex, idx, data[idx]);
            }
        }
    }
    
    /** helper for <code>addHist(frameIndex, idx, value)</code> */
    public void addHist(int frameIndex, int x, int y, int value) {
        int idx = y * dim.height + x;
        addTimeValue(frameIndex, idx, value);
    }
    
    public void addTimeValue(int frameIndex, int idx, int value) {
        final int baseAddr =  idx * histSlotSize;
        int startModulo = data[baseAddr+BASE_OFFSET_startModulo];
        int histAddr = baseAddr + 2 + startModulo * 2;
        int prevValue = data[histAddr + OFFSET_prevValue];
        // int changeFrameIndex = data[indexHistModulo+1];
        if (prevValue != value) {
            int newStartModulo = FastModuloUtils.incrModulo(startModulo, historyLen);
            data[baseAddr + BASE_OFFSET_countChange]++;
            data[baseAddr + BASE_OFFSET_startModulo] = newStartModulo;
            
            int newHistAddr = baseAddr + 2 + newStartModulo * 2;
            data[newHistAddr + OFFSET_prevValue] = value;
            data[newHistAddr + OFFSET_prevFrameIndex] = frameIndex;
        }
    }
    

    public int getCountChange(int idx) {
        final int baseIndex =  idx * histSlotSize;
        return data[baseIndex+BASE_OFFSET_countChange];
    }

    public int getNthPrevValue(int idx, int n) {
        if (0 > n || n >= historyLen) {
            throw new IllegalArgumentException();
        }
        final int baseAddr =  idx * histSlotSize;
        int startModulo = data[baseAddr+BASE_OFFSET_startModulo];
        int prevInternalIndexModulo = FastModuloUtils.minusModulo(startModulo, n, historyLen);
        // inline... return _getPrevValue(idx, prevInternalIndexModulo);
        int histAddr = baseAddr + 2 + prevInternalIndexModulo * 2;
        return data[histAddr + OFFSET_prevValue];
    }

    public int getNthPrevFrameIndex(int idx, int n) {
        if (0 > n || n >= historyLen) {
            throw new IllegalArgumentException();
        }
        final int baseAddr =  idx * histSlotSize;
        int startModulo = data[baseAddr+BASE_OFFSET_startModulo];
        int prevInternalIndexModulo = FastModuloUtils.minusModulo(startModulo, n, historyLen);
        // inline... return _getPrevFrameIndex(idx, prevInternalIndexModulo);
        int histAddr = baseAddr + 2 + prevInternalIndexModulo * 2;
        return data[histAddr + OFFSET_prevFrameIndex];
    }

    public int _startModulo(int idx) {
        final int baseAddr =  idx * histSlotSize;
        return data[baseAddr + BASE_OFFSET_startModulo];
    }
    
    public int _nthPrevInternalModuloIndex(int idx, int n) {
        return FastModuloUtils.minusModulo(_startModulo(idx), n, historyLen);
    }

    public int _getPrevValue(int idx, int internalIndexModulo) {
        final int baseAddr =  idx * histSlotSize;
        int histAddr = baseAddr + 2 + internalIndexModulo * 2;
        return data[histAddr + OFFSET_prevValue];
    }

    public int _getPrevFrameIndex(int idx, int internalIndexModulo) {
        final int baseAddr =  idx * histSlotSize;
        int histAddr = baseAddr + 2 + internalIndexModulo * 2;
        return data[histAddr + OFFSET_prevFrameIndex];
    }

    
}
