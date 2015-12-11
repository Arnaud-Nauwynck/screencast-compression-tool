package fr.an.screencast.compressor.imgtool.delta;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import fr.an.screencast.compressor.imgtool.utils.FastModuloUtils;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.Rect;

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

    public void addTimeValues(int frameIndex, BufferedImage data) {
        addTimeValues(frameIndex, ImageRasterUtils.toInts(data));
    }
    
    public void addTimeValues(int frameIndex, int[] data) {
        final int height = dim.height, width = dim.width;
        for(int y = 0, idx=0; y < height; y++) {
            for(int x = 0; x < width; x++, idx++) {
                addTimeValue(frameIndex, idx, data[idx]);
            }
        }
    }

    public void addTimeValues(int frameIndex, int[] data, Rect rect) {
        final int width = dim.width;
        int incrIdxY = dim.width - rect.toX + rect.fromX;
        for(int y = rect.fromY, idx=rect.fromY*width+rect.fromX; y < rect.toY; y++,idx+=incrIdxY) {
            // idx=y*width+rect.fromX; //TODO OPTIM
            ImageRasterUtils.checkIdx(idx, rect.fromX, y, width);
            for(int x = rect.fromX; x < rect.toX; x++, idx++) {
                addTimeValue(frameIndex, idx, data[idx]);
            }
        }
    }

    /** helper for <code>addHist(frameIndex, idx, value)</code> */
    public void addTimeValue(int frameIndex, int x, int y, int value) {
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

    public static class FrameIndexPrevValue {
        int frameIndex;
        int prevValue;
    }

    public FrameIndexPrevValue getNthChange(FrameIndexPrevValue res, int idx, int n) {
        if (0 > n || n >= historyLen) {
            throw new IllegalArgumentException();
        }
        if (res == null) res = new FrameIndexPrevValue();
        final int baseAddr =  idx * histSlotSize;
        int startModulo = data[baseAddr+BASE_OFFSET_startModulo];
        int prevInternalIndexModulo = FastModuloUtils.minusModulo(startModulo, n, historyLen);
        int histAddr = baseAddr + 2 + prevInternalIndexModulo * 2;
        res.prevValue = data[histAddr + OFFSET_prevValue];
        res.frameIndex = data[histAddr + OFFSET_prevFrameIndex];
        return res;
    }
    
    public int _startModulo(int idx) {
        final int baseAddr =  idx * histSlotSize;
        return data[baseAddr + BASE_OFFSET_startModulo];
    }
    
    public int _nthPrevInternalModuloIndex(int idx, int n) {
        return FastModuloUtils.minusModulo(_startModulo(idx), n, historyLen);
    }

    public void _getFramePrevValue(FrameIndexPrevValue res, int idx, int internalIndexModulo) {
        final int baseAddr =  idx * histSlotSize;
        int histAddr = baseAddr + 2 + internalIndexModulo * 2;
        res.prevValue = data[histAddr + OFFSET_prevValue];
        res.frameIndex = data[histAddr + OFFSET_prevFrameIndex];
    }

    public FrameIndexPrevValue findPrevFrameIndex(FrameIndexPrevValue res, int idx, int frameIndex) {
        if (res == null) res = new FrameIndexPrevValue();
        final int baseAddr =  idx * histSlotSize;
        final int startModulo = data[baseAddr+BASE_OFFSET_startModulo];
        final int countChange = data[baseAddr+BASE_OFFSET_countChange];
        // loop from  startModulo .. downto 0 
        for(int prevModulo = startModulo; prevModulo >= 0; prevModulo--) {
            _getFramePrevValue(res, idx, prevModulo);
            if (res.frameIndex <= frameIndex) {
                res.frameIndex = frameIndex; // overwrite (no return information to caller)
                return res;
            }
        }
        // loop from min(coutChange,N-1) .. downto startModulo+1
        for(int prevModulo = Math.min(historyLen-1, countChange); prevModulo > startModulo; prevModulo--) {
            _getFramePrevValue(res, idx, prevModulo);
            if (res.frameIndex <= frameIndex) {
                res.frameIndex = frameIndex;
                return res;
            }
        }
        res.frameIndex = -1;
        res.prevValue = 0;
        return res;
    }
    
    // ------------------------------------------------------------------------

    public static class RectRestorableResult {
        int frameIndex;
        Pt prevFrameLocation;
        int countDiff;
        int countUnrestorable;
        
        public RectRestorableResult(int frameIndex, Pt prevFrameLocation, int countDiff, int countUnrestorable) {
            this.frameIndex = frameIndex;
            this.prevFrameLocation = prevFrameLocation;
            this.countDiff = countDiff;
            this.countUnrestorable = countUnrestorable;
        }

        @Override
        public String toString() {
            return "RectRestorableResult [countDiff=" + countDiff
                + ", countUnrestorable=" + countUnrestorable 
                + ", frameIndex=" + frameIndex + ", prevFrameLocation=" + prevFrameLocation 
                + "]";
        }
        
        
    }
    
    /**
     * @param imageData
     * @param rect
     * @return count of diff values
     */
    public List<RectRestorableResult> computeRestorableNthPrevFrame(int currentFrameIndex, int[] imgData, Rect rect, Pt prevFrameLocation, 
            int diffThreshold, int unrestorableThreshold
            ) {
        List<RectRestorableResult> res = new ArrayList<RectRestorableResult>();
        
//        int bestCountDiffSoFar = Integer.MAX_VALUE;
//        int bestScoreSoFar = Integer.MAX_VALUE;
        for(int prevFrameIndex = currentFrameIndex-1; ; prevFrameIndex--) {
            RectRestorableResult tmpres = computeRestorableRectFrame(prevFrameIndex, imgData, rect, prevFrameLocation, 
                    diffThreshold, unrestorableThreshold);

            if (tmpres.countDiff == 0 && tmpres.countUnrestorable == 0) {
                res.add(tmpres);
                break; // found exact restore, finished
            }

            if (tmpres.countUnrestorable > unrestorableThreshold) {
                break; // stop.. (results will be worse and worse)
            }
            
            // TODO return all or only best per score? (smaller countDiff, then smaller countUnrestorable) 
            res.add(tmpres);

//            if (bestCountDiffSoFar > countDiff) {
//                bestCountDiffSoFar = countDiff;
//            }
//            int score = countDiff + countUnrestorable;
//            if (bestScoreSoFar > score) {
//                bestScoreSoFar = score;
//            }
        }
        return res;
    }

    public RectRestorableResult computeRestorableRectFrame(int prevFrameIndex, int[] imgData, Rect rect, Pt prevFrameLocation, 
            int diffThreshold, int unrestorableThreshold
            ) {
        final int width = dim.getWidth();
        final int incrIdxY = width + rect.fromX - rect.toX; 
        final FrameIndexPrevValue tmpFramePrevValue = new FrameIndexPrevValue();

        int idx = rect.fromY*width + rect.fromX;
        int prevIdx = prevFrameLocation.y*width + prevFrameLocation.x;
        int countDiff = 0;
        int countUnrestorable = 0;
        loop_y: for(int y = rect.fromY; y < rect.toY; y++,idx+=incrIdxY,prevIdx+=incrIdxY) {
            for (int x = rect.fromX; x < rect.toX; x++,idx++,prevIdx++) {
                findPrevFrameIndex(tmpFramePrevValue, prevIdx, prevFrameIndex);
                if (tmpFramePrevValue.frameIndex != -1) {
                    if (imgData[idx] != tmpFramePrevValue.prevValue) {
                        countDiff++;
                        if (countDiff > diffThreshold) {
                            break loop_y;
                        }
                    }
                } else {
                    countUnrestorable++;
                    if (countUnrestorable > unrestorableThreshold) {
                        break loop_y;
                    }
                }
            }
        }
        return new RectRestorableResult(prevFrameIndex, prevFrameLocation, countDiff, countUnrestorable);
    }
    
    /**
     * @param imgData
     * @param frameIndex
     * @param rect
     * @param prevFrameLocation
     * @return count of unrestored pixel values
     */
    public int tryRestoreFrameImageRect(int[] imgData, int frameIndex, Rect rect, Pt prevFrameLocation) {
        int countUnrestored = 0;
        final int width = dim.getWidth();
        final int incrIdxY = width + rect.fromX - rect.toX; 
        FrameIndexPrevValue tmpFramePrevValue = new FrameIndexPrevValue();
        
        int idx = rect.fromY*width + rect.fromX;
        int prevIdx = prevFrameLocation.y*width + prevFrameLocation.x;
        for(int y = rect.fromY; y < rect.toY; y++,idx+=incrIdxY,prevIdx+=incrIdxY) {
            for (int x = rect.fromX; x < rect.toX; x++,idx++,prevIdx++) {
                findPrevFrameIndex(tmpFramePrevValue, prevIdx, frameIndex);
                if (tmpFramePrevValue.frameIndex != -1) {
                    imgData[idx] = tmpFramePrevValue.prevValue;
                } else {
                    countUnrestored++;
                }
            }
        }
        return countUnrestored;
    }
    
}
