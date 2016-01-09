package fr.an.screencast.compressor.imgstream.codecs.deltabitstream;

import fr.an.bitwise4j.encoder.huffman.HuffmanTable;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;

/** 
 * data struct equivalent to "ColorBgSegment[]" with big size (typical: ~1M for img 1920x1080) 
 * where
 * <code>
 * public static abstract class ColorBgSegment {
 *   public abstract int getPos();
 *   public abstract int getColor();
 *   public abstract int getLen();
 *   public abstract int getBgLen();
 * }
 * </code> 
 */
public class ColorBgSegmentArray {
    
    private static final int OFFSET_P0S = 0;
    private static final int OFFSET_LEN = 1;
    private static final int OFFSET_COLOR = 2;
    private static final int OFFSET_BG_LEN = 3;
    
    private int size;
    private int[] data = new int[4096*8];
    
    HuffmanTable<Integer> fgColorHuffmanTable = new HuffmanTable<Integer>();
    int minSegmentLen;
    int maxSegmentLen;
    int minSegmentBgLen;
    int maxSegmentBgLen;
    int minSegmentLenPlusBgLen;
    int maxSegmentLenPlusBgLen;
    
    private int sumSegmentBgLen;
    
    // ------------------------------------------------------------------------

    public ColorBgSegmentArray() {
    }

    // ------------------------------------------------------------------------
    
    public void addSegment(int pos, int len, int color, int bgLen) {
        int idx = size*4;
        if ((idx+4) > data.length) {
            // realloc
            int[] tmp = new int[data.length + 12 + data.length/4];
            System.arraycopy(data,  0, tmp,  0, data.length);
            data = tmp;
        }
        data[idx + OFFSET_P0S] = pos;
        data[idx + OFFSET_LEN] = len;
        data[idx + OFFSET_COLOR] = color;
        data[idx + OFFSET_BG_LEN] = bgLen;
        size++;
    }
    
    public int size() {
        return size;
    }
    public static int iterAt(int i) {
        return i*4;
    }
    public static int next(int iter) {
        return iter+4;
    }
    public static int iterIncr() {
        return 4;
    }
    public int maxIter() {
        return size*4;
    }

    public int getPos(int iter) {
        return data[iter + OFFSET_P0S]; 
    }
    public int getColor(int iter) {
        return data[iter + OFFSET_COLOR]; 
    }
    public int getLen(int iter) {
        return data[iter + OFFSET_LEN]; 
    }
    public int getBgLen(int iter) {
        return data[iter + OFFSET_BG_LEN]; 
    }
    

    public void computeSegmentsForImgAndBgColor(int[] imgData, int bgColor) {
        this.size = 0;
        this.sumSegmentBgLen = 0;
        
        final int len = imgData.length;
        int idx = 0;
        // iterate on BgColor  (until color != BgColor)
        for(; idx < len; idx++) {
            if (bgColor != imgData[idx]) {
                break;
            }
        }
        if (idx != 0) {
            addSegment(0, 0, -1, idx);
        }
        
        for(; idx < len;) {
            final int segFromPos = idx;
            final int currColor = imgData[idx];
            // iterate until color != currColor
            idx++;
            for(; idx < len; idx++) {
                if (currColor != imgData[idx]) {
                    break;
                }
            }
            final int startBgIdx = idx; 
            // iterate in Bg
            for(; idx < len; idx++) {
                if (bgColor != imgData[idx]) {
                    break;
                }
            }
            final int bgLen = idx-startBgIdx;
            sumSegmentBgLen += bgLen;
            
            addSegment(segFromPos, startBgIdx-segFromPos, currColor, bgLen);
        }
    }
    
    public void computeSegmentsHuffmanTableAndMinMaxLens() {
        // compute huffman table of color frequency per segments (not per absolute count)
        // also compute min/max for varLengthLen[.] and varLengthBgLen[.]
        this.fgColorHuffmanTable.clear();

        // compute min,max ... do not take getLen(0) into account??
        this.minSegmentLen = Integer.MAX_VALUE;
        this.maxSegmentLen = Integer.MIN_VALUE;
        this.minSegmentBgLen = this.maxSegmentBgLen = getBgLen(0);
        this.minSegmentLenPlusBgLen = Integer.MAX_VALUE;
        this.maxSegmentLenPlusBgLen = Integer.MIN_VALUE;

        final int segmentIterMax = maxIter();
        for(int iter=iterAt(1); iter < segmentIterMax; iter+=iterIncr()) {
            final int color = getColor(iter);
            fgColorHuffmanTable.incrSymbolFreq(color, 1);
            
            final int segmentLen = getLen(iter);
            final int segmentBgLen = getBgLen(iter);
            final int segmentLenPlusBgLen = segmentLen+segmentBgLen;
            this.minSegmentLen = Math.min(minSegmentLen, segmentLen);
            this.maxSegmentLen = Math.max(maxSegmentLen, segmentLen);
            this.minSegmentBgLen = Math.min(minSegmentBgLen, segmentBgLen);
            this.maxSegmentBgLen = Math.max(maxSegmentBgLen, segmentBgLen);
            this.minSegmentLenPlusBgLen = Math.min(minSegmentLenPlusBgLen, segmentLenPlusBgLen);
            this.maxSegmentLenPlusBgLen = Math.max(maxSegmentLenPlusBgLen, segmentLenPlusBgLen);
        }
        fgColorHuffmanTable.compute();
    }

    @Override
    public String toString() {
        return "ColorBgSegmentArray [size:" + size 
                + ", fgColorHuffmanTable=" + fgColorHuffmanTable.toStringDump(x -> RGBUtils.toString(x)) 
                + ", minSegmentLen=" + minSegmentLen 
                + ", maxSegmentLen=" + maxSegmentLen 
                + ", minSegmentBgLen=" + minSegmentBgLen 
                + ", maxSegmentBgLen=" + maxSegmentBgLen 
                + ", minSegmentLenPlusBgLen=" + minSegmentLenPlusBgLen 
                + ", maxSegmentLenPlusBgLen=" + maxSegmentLenPlusBgLen 
                + "]";
    }
 
    
    
}