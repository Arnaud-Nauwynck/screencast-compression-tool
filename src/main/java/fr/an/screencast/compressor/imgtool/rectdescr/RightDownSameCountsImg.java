package fr.an.screencast.compressor.imgtool.rectdescr;

import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Rect;

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
        this.rightSameCounts = new int[dim.getArea()];
        this.downSameCounts = new int[dim.getArea()];
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
 
    public void setComputeFromUniformImg() {
        final int W = dim.width, H = dim.height;
        for(int y = 0, idx = 0; y < H; y++) {
            int x = 0;
            ImageRasterUtils.checkIdx(idx, x, y, W);
            for(; x < W; x++,idx++) {
                rightSameCounts[idx] = W - x;
            }
        }
        for(int x = 0, idx = 0; x < W; x++,idx=x) {
            for(int y = 0; y < H; y++,idx+=W) {
                downSameCounts[idx] = H - y;
            }
        }
    }

    public void updateDiffCountsRect(Rect rect) {
        final int W = dim.width;
        final int fromX = rect.fromX, toX = rect.toX, fromY = rect.fromY, toY = rect.toY; 
        // final int rectW = toX - fromX, rectH = toY - fromY;
        int x = fromX, y = fromY, idx = fromY * W + fromX;
        
        // step 1: update rightSameCounts[]
        for(y = fromY; y < toY; y++,idx+=W) {
            x = fromX;
            if (ImageRasterUtils.CHECK) ImageRasterUtils.checkIdx(idx, x, y, W);
            updateRightSameCountForSegment(fromX, toX, idx);
        }

        // step 1: update downSameCounts[]
        x = fromX; y = fromY; idx = fromY * W + fromX;
        // final int idxFromY = fromY * W;
        for(; x < toX; x++,y=fromY,idx=fromY * W + x) {
            // if (ImageRasterUtils.CHECK) ImageRasterUtils.checkIdx(idx, x, y, W);
            // determine upper-most break point
            updateDownSameCountVertSegment(fromY, toY, idx);
        }
    }

    private void updateDownSameCountVertSegment(final int fromY, final int toY, int idx) {
        final int W = dim.width;
        int y;
        int upY = fromY;
        int idxUp = idx;
        int countUp = 0;
        for (; upY-1 >= 0 && downSameCounts[idxUp-W] > countUp+1; upY--,idxUp-=W,countUp++) {
        }
        // update on up
        y = upY; idx = idxUp;
        for(; y < fromY; y++,idx+=W) {
            downSameCounts[idx] = countUp--; 
        }
        // update within rect
        for(y = fromY; y < toY; y++,idx+=W) {
            downSameCounts[idx] = toY - y;
        }
    }


    public void updateDiffCountsSegment(int fromX, int toX, int y) {
        final int W = dim.width;
        int idx = y * W + fromX;
        updateRightSameCountForSegment(fromX, toX, idx);
        for (int x = fromX; x < toX; x++,idx++) {
            updateDownSameCountVertSegment(y, y+1, idx);
        }
    }
    
    
    
    private void updateRightSameCountForSegment(final int fromX, final int toX, int idx) {
        int x;
        // determine left-most break point
        //       fromX       toX
        //         [         ( 
        // . 1 6 5 4 3 2 1 . . . 
        //     |<---
        //   leftX
        // =>      | 
        //     2 1[5 4 3 2 1 ( ...
        int leftX = fromX;
        int idxLeftX = idx;
        int countLeft = 0;
        for (; leftX-1 >= 0 && rightSameCounts[idxLeftX-1] > countLeft+1; leftX--,idxLeftX--,countLeft++) {
        }
        // update on left
        x = leftX; idx = idxLeftX;
        for(; x < fromX; x++,idx++) {
            rightSameCounts[idx] = countLeft--; 
        }
        // update within rect
        for(; x < toX; x++,idx++) {
            rightSameCounts[idx] = toX - x;
        }
    }
    
    
    
    public int getRightSameCount(int idx) {
        int res = rightSameCounts[idx];
        return res;
    }

    public int getDownSameCount(int idx) {
        int res = downSameCounts[idx];
        return res;
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
