package fr.an.screencast.compressor.imgtool.color;

import java.util.Arrays;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

/**
 * a global lookup table for converting RGB -> int, and revert int -> RGB
 * where all index are used (dense array)
 */
public class ColorLookupTable {

    private Int2IntMap rgbToIndexMap;
    private int[] indexToRgb;
    
    private static final int NO_COLOR = -2;
    
    // ------------------------------------------------------------------------

    public ColorLookupTable(int allocSize) {
        this.rgbToIndexMap = new Int2IntOpenHashMap(allocSize);
        this.indexToRgb = new int[allocSize];
        
        rgbToIndexMap.defaultReturnValue(NO_COLOR);
    }

    // ------------------------------------------------------------------------

    public int registerRGB(int rgb) {
        int res = rgbToIndexMap.get(rgb);
        if (res == NO_COLOR) {
            res = rgbToIndexMap.size(); // from 0 to size() excluded
            rgbToIndexMap.put(rgb, res);
            if (res >= indexToRgb.length) {
                indexToRgb = Arrays.copyOf(indexToRgb, indexToRgb.length + 128);
            }
            indexToRgb[res] = rgb;
        }
        return res;
    }

    public void registerRGBs(int[] src) {
        registerRGBs(src, 0, src.length);
    }
    
    public void registerRGBs(int[] src, int from, int to) {
        int prevColor = src[from];
        registerRGB(prevColor);
        for(int i = from+1; i < to; i++) {
            int currColor = src[i];
            if (currColor != prevColor) {
                registerRGB(currColor);
            }
            currColor = prevColor;
        }
    }

    public int rgb2Index(int rgb) {
        return rgbToIndexMap.get(rgb);
    }

    public int index2rgb(int index) {
        return indexToRgb[index];
    }
    
    public int size() {
        return rgbToIndexMap.size();
    }
    
}
