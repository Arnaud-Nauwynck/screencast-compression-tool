package fr.an.screencast.compressor;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class DeltaImageAnalysis {

    private static final boolean DEBUG = true;
    private static final boolean DEBUG_DIFF_LINE = 
            true;
//        false;
    
    private final int width;
    private final int height;
    
    private int[] prevData;
    private int[] data;
    
    private int[] diffCountIntegral;
    
    private int countDiffLine;
    private int[] j_firstDiff_i;
    private int[] j_lastDiff_i;
    private Rectangle diffRect;
    
    private List<Rectangle> diffRects = new ArrayList<Rectangle>();
    
    public DeltaImageAnalysis(int width, int height, int[] prevData, int[] data) {
        this.width = width;
        this.height = height;
        this.prevData = prevData;
        this.data = data;

        diffCountIntegral = new int[width * height]; 
        j_firstDiff_i = new int[height];
        j_lastDiff_i = new int[height];
    }
    
    public void setData(int[] prevData, int[] data) {
        this.prevData = prevData;
        this.data = data;
    }
    
    public void computeDiff() {
        final int[] prevData = this.prevData;
        final int[] data = this.data;
        
        int INVALID_FIRST = Integer.MAX_VALUE; 
        int INVALID_LAST = Integer.MIN_VALUE;
        int pos_ij = 0;
        int pos_i = 0;
        for(int i = 0; i < height; i++,pos_i+=width) {
            pos_ij = pos_i;
            int j_firstDiff = INVALID_FIRST;
            for(int j = 0; j < width; j++, pos_ij++) {
                if (data[pos_ij] != prevData[pos_ij]) {
                    j_firstDiff = j;
                    break;
                }
            }
        
            int j_lastDiff = INVALID_LAST;
            if (j_firstDiff != INVALID_FIRST) {
                pos_ij = pos_i + width-1;
                for(int j = width-1; j >= j_firstDiff; j--, pos_ij--) {
                    if (data[pos_ij] != prevData[pos_ij]) {
                        j_lastDiff = j; // +1?
                        break;
                    }
                }
            }
        
            j_firstDiff_i[i] = j_firstDiff;
            j_lastDiff_i[i] = j_lastDiff;
        }
        
        int i_firstDiff = INVALID_FIRST;
        int i_lastDiff = INVALID_LAST;
        int j_firstDiff = INVALID_FIRST;
        int j_lastDiff = INVALID_LAST;
        countDiffLine = 0;
        for (int i = 0; i < height; i++) {
            if (j_firstDiff_i[i] != INVALID_FIRST) {
                countDiffLine++;
                if (i_firstDiff == INVALID_FIRST) {
                    i_firstDiff = i;
                }
                if (i > i_lastDiff) {
                    i_lastDiff = i;
                }
                j_firstDiff = Math.min(j_firstDiff, j_firstDiff_i[i]);
                j_lastDiff = Math.max(j_lastDiff, j_lastDiff_i[i]);
            }
        }
        
        diffRect = new Rectangle();
        diffRect.setBounds(j_firstDiff, i_firstDiff, 
                j_lastDiff!=INVALID_LAST? (j_lastDiff-j_firstDiff+1):0,
                i_lastDiff!=INVALID_LAST? (i_lastDiff-i_firstDiff+1):0 
                );
       // countDiffLine
        
        diffRects.clear();
        if (countDiffLine != 0) {
            int prevDiffLine = 0;
            int currDiffFirstLine = INVALID_FIRST;
            int currDiffMinJ = INVALID_FIRST;
            int currDiffMaxJ = INVALID_LAST;
            if (DEBUG) System.out.println(" countDiffLine:" + countDiffLine + " in rect: " + i_firstDiff + ".." + i_lastDiff + " x " + j_firstDiff + ".." + j_lastDiff);
            for (int i = 0; i < height; i++) {
                if (j_firstDiff_i[i] != INVALID_FIRST) {
                    if (prevDiffLine+1 != i) {
                        if (currDiffFirstLine != INVALID_FIRST) {
                            Rectangle r = new Rectangle(currDiffMinJ, currDiffFirstLine, currDiffMaxJ-currDiffMinJ, i-1-currDiffFirstLine); 
                            if (DEBUG) System.out.println("  ... diff rect: " + r);
                    
                            diffRects.add(r);
                        }
                        if (DEBUG_DIFF_LINE) System.out.println("  (no h diff) -----------------");
                        currDiffFirstLine = INVALID_FIRST;
                        currDiffMinJ = INVALID_FIRST;
                        currDiffMaxJ = INVALID_LAST;
                    }
                    if (DEBUG_DIFF_LINE) System.out.println(" diff " + i + " : " + j_firstDiff_i[i] + " " + j_lastDiff_i[i]);
                    if (currDiffFirstLine == INVALID_FIRST) currDiffFirstLine = i;
                    currDiffMinJ = Math.min(currDiffMinJ, j_firstDiff_i[i]);
                    currDiffMaxJ = Math.max(currDiffMaxJ, j_lastDiff_i[i]);
                    
                    prevDiffLine = i;
                }
            }
            if (DEBUG) System.out.println("");
        }
    }

    public int getCountDiffLine() {
        return countDiffLine;
    }

    public int[] getJ_firstDiff_i() {
        return j_firstDiff_i;
    }

    public int[] getJ_lastDiff_i() {
        return j_lastDiff_i;
    }

    public Rectangle getDiffRect() {
        return diffRect;
    }

    public List<Rectangle> getDiffRects() {
        return diffRects;
    }
        
}
