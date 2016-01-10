package fr.an.screencast.compressor.imgtool.rectdescr;

import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.utils.Dim;

/**
 * img data structure for var-length counter of similar pixels on right & down directions
 *
 */
public final class RightDownSameCountsImg {

    private final Dim dim;
    
    private final int[] rightSameCounts;
    private final int[] downSameCounts;
    
    // ------------------------------------------------------------------------
    
    public RightDownSameCountsImg(Dim dim) {
        this.dim = dim;
        rightSameCounts = new int[dim.getArea()];
        downSameCounts = new int[dim.getArea()];
    }

    // ------------------------------------------------------------------------

    public void setComputeFrom(int[] src) {
        final int W = dim.width, H = dim.height;
        // step 1: scan y,x => compute rightSameCounts[x,y].. set + fast increment x
        for(int y = 0, idx = 0; y < H; y++) {
            int x = 0;
            ImageRasterUtils.checkIdx(idx, x, y, W);
            for(; x < W; x++,idx++) {
                ImageRasterUtils.checkIdx(idx, x, y, W);
                final int currColor = src[idx];
                // compute next changed color pixel on right
                int toX = x + 1, toIdx = idx + 1; 
                for (; toX < W; toX++,toIdx++) {
                    if (currColor != src[toIdx]) {
                        break;
                    }
                }
                // apply computed len on [x, toX( 
                for (int currLen = toX - x; currLen >= 1; currLen--,x++,idx++) {
                    rightSameCounts[idx] = currLen;
                }
                x = toX-1;
                idx--;
                ImageRasterUtils.checkIdx(idx, x, y, W);
            }
        }
        // step 2: (same on transpose img) scan x,y => compute downSameCounts[x,y] .. set + fast increment y
        for(int x = 0, idx = 0; x < W; x++,idx=x) {
            for(int y = 0; y < H; y++,idx+=W) {
                final int currColor = src[idx];
                // compute next changed color pixel on down
                int toY = y + 1, toIdx = idx + W; 
                for (; toY < H; toY++,toIdx+=W) {
                    if (currColor != src[toIdx]) {
                        break;
                    }
                }
                // apply computed len on [y, toY( 
                for (int currLen = toY - y; currLen >= 1; currLen--,y++,idx+=W) {
                    downSameCounts[idx] = currLen;
                }
                y = toY-1;
                idx-=W;
                ImageRasterUtils.checkIdx(idx, x, y, W);
            }
        }
    }
 
    
    public int getRightSameCount(int idx) {
        return rightSameCounts[idx];
    }

    public int getDownSameCount(int idx) {
        return downSameCounts[idx];
    }

    public int[] getRightSameCounts() {
        return rightSameCounts;
    }

    public int[] getDownSameCounts() {
        return downSameCounts;
    }

    public int getLeftSameCount(int x, int y, int idx) {
        int toX = x;
        int len = 1;
        for(; toX >= 0 && rightSameCounts[idx] >= len; toX--,idx--,len++) {
        }
        return len;
    }

}
