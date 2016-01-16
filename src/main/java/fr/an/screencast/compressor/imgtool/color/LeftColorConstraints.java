package fr.an.screencast.compressor.imgtool.color;

import java.util.Arrays;

import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.imgtool.utils.IntsUtils;
import fr.an.screencast.compressor.utils.Dim;

/**
 * data-structure for list of possible colors given leftColor of point
 * 
 * 
 */
public class LeftColorConstraints {
    
    private ColorLookupTable colorLookupTable;

    private static final int[] EMPTY_COLS = new int[0];
    
    private static class ColorPossibilities implements Comparable<ColorPossibilities> {
        private final int leftColorIndex;
        
        int colorIndexesCount = 0;
        int[] colorIndexes = EMPTY_COLS;
        // int[] frequencyCount;
        
        public ColorPossibilities(int leftColorIndex) {
            this.leftColorIndex = leftColorIndex;
        }
        
        public int findColorPos(int value) {
            return Arrays.binarySearch(colorIndexes, 0, colorIndexesCount, value);
        }

        public int findOrInsertColor(int value) {
            int pos = findColorPos(value);
            if (pos < 0) {
                pos = -pos-1;
                colorIndexes = IntsUtils.insert(colorIndexes, colorIndexesCount, pos, value);
                colorIndexesCount++;
            }
            return pos;
        }
        
        
        @Override
        public int compareTo(ColorPossibilities other) {
            int res = - Integer.compare(colorIndexesCount, other.colorIndexesCount);
            if (res == 0) {
                res = Integer.compare(leftColorIndex, other.leftColorIndex);
            }
            return res;
        }

        public String toString() {
            return leftColorIndex + "=>" + colorIndexesCount;
        }
        
    }

    private ColorPossibilities[] rightPossibilities;
    
    // ------------------------------------------------------------------------

    public LeftColorConstraints(ColorLookupTable colorLookupTable) {
        this.colorLookupTable = colorLookupTable;
        final int size = colorLookupTable.size();
        rightPossibilities = new ColorPossibilities[size];
        for(int leftColorIndex = 0; leftColorIndex < size; leftColorIndex++) {
            rightPossibilities[leftColorIndex] = new ColorPossibilities(leftColorIndex);
        }
    }

    // ------------------------------------------------------------------------

    public void analyseImgColors(Dim dim, int[] imgData) {
        final int W = dim.getWidth(), H = dim.getHeight();
        int y = 0, x = 0, idx = 0;
        int leftRGBIdx, currRGBIdx;
        // first line, first col
        currRGBIdx = colorLookupTable.rgb2Index(imgData[idx]);
        leftRGBIdx = currRGBIdx; // dummy left&up color
        registerLeftCurrColorIdx(leftRGBIdx, currRGBIdx);
        
        // first line
        int prevColorIdx = currRGBIdx; 
        for(x++,idx++; x < W; x++,idx++) {
            leftRGBIdx = prevColorIdx;
            ImageRasterUtils.checkIdx(idx, x, y, W);
            currRGBIdx = colorLookupTable.rgb2Index(imgData[idx]);
            registerLeftCurrColorIdx(leftRGBIdx, currRGBIdx);
            prevColorIdx = currRGBIdx; 
        }
        // ImageRasterUtils.checkIdx(idx, x, y, W);
        for(y = 1; y < H; y++) {
            // first col
            x = 0;
            currRGBIdx = colorLookupTable.rgb2Index(imgData[idx]);
            leftRGBIdx = currRGBIdx; //dummy left color
            registerLeftCurrColorIdx(leftRGBIdx, currRGBIdx);
            
            // remaining cols
            prevColorIdx = currRGBIdx;
            for(x++, idx++; x < W; x++,idx++) {
                // ImageRasterUtils.checkIdx(idx, x, y, W);
                leftRGBIdx = prevColorIdx; // = colorLookupTable.rgb2Index(imgData[idx-1]);
                currRGBIdx = colorLookupTable.rgb2Index(imgData[idx]);

                registerLeftCurrColorIdx(leftRGBIdx, currRGBIdx);
                prevColorIdx = currRGBIdx;
            }
        }
    }

    private void registerLeftCurrColorIdx(int leftRGBIdx, int currRGBIdx) {
        ColorPossibilities rightPoss = rightPossibilities[leftRGBIdx];
        rightPoss.findOrInsertColor(currRGBIdx);
    }
    
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // sort by possibilities count
        ColorPossibilities[] sorted = Arrays.copyOf(rightPossibilities, rightPossibilities.length);
        Arrays.sort(sorted);
        int prevCount = -1; // sorted[0].colorIndexesCount;
        int countWithSameLen = 0;
        for(ColorPossibilities e : sorted) {
            if (prevCount == e.colorIndexesCount) {
                if (countWithSameLen > 0) {
                    sb.append(", ");
                }
                countWithSameLen++;
            } else {
                if (countWithSameLen > 1) {
                    sb.append(" .. " + countWithSameLen + " elt(s)");
                }
                sb.append("\n");
                prevCount = e.colorIndexesCount;
                countWithSameLen = 1;
                sb.append(prevCount + " right color possibilities for : ");
            }
            sb.append(e.leftColorIndex);
            if (e.colorIndexesCount <= 1) {
                break; // skip display all remaining with implicit count=1
            }
        }
        return sb.toString();
    }
}
