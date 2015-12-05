package fr.an.screencast.compressor.imgtool.delta;

import java.util.ArrayList;
import java.util.List;

import fr.an.screencast.compressor.imgtool.utils.ImageData;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.imgtool.utils.RasterImageFunction;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.QuadDirection;
import fr.an.screencast.compressor.utils.Rect;

public class BinaryImageEnclosingRectsFinder {

    private static final boolean CHECK_IDX = true;
    
    private int initialDilateRect = 10;

    private int dilateRect = 10;
    
    private final Dim dim;
    
    private ImageData remainDiffData;

    private Pt firstDiffPt = new Pt();
    
    // ------------------------------------------------------------------------
    
    public BinaryImageEnclosingRectsFinder(Dim dim) {
        this.dim = dim;
        this.remainDiffData = new ImageData(dim);
    }

    // ------------------------------------------------------------------------
    
    public List<Rect> findEnclosingRects(RasterImageFunction binaryFunction) {
        List<Rect> res = new ArrayList<Rect>();
        remainDiffData.set(binaryFunction);

        Pt findFromPt = new Pt(0, 0);
        Pt diffPt = new Pt();
        
        firstDiffPt.set(-1, -1);
        
        for(;;) {
            if (! findFirstDiffPt(diffPt, findFromPt)) {
                break; 
            }

            if (firstDiffPt.x == -1) {
                firstDiffPt.set(diffPt);
            }
            // starting rect enclosing diff point 
            Rect rect = Rect.newPtDim(diffPt, 1, 1);
            // dilatation of rect to avoid small isolated pixel rects
            rect.fromX = Math.max(0, rect.fromX - initialDilateRect);
            rect.toX = Math.min(dim.width, rect.toX + initialDilateRect);
            rect.toY = Math.min(dim.height, rect.toY + initialDilateRect*2);
    
            // try increase rectange on left,down,right until enclosed by non-diff area
            dilateRectUntilNoMoreDiff(rect);
            
//            // erode rect and bound to [width,height]
//            rect.fromX = Math.max(0, rect.fromX - dilateRect);
//            rect.toX = Math.min(dim.width, rect.toX + dilateRect);
//            rect.toY = Math.min(dim.height, rect.toY - initialDilateRect);
            rect.fromX = Math.max(0, rect.fromX);
            rect.toX = Math.min(dim.width, rect.toX);
            rect.toY = Math.min(dim.height, rect.toY);
            
            
            remainDiffData.setFillRect(rect, 0);

            res.add(rect);
            
            findFromPt.y = diffPt.y;
            findFromPt.x = rect.toX;
            if (! findFromPt.setNextHorizontalScan(dim)) {
                break; // reached end of image
            }
        }
        
        return res;
    }


    private boolean findFirstDiffPt(Pt res, Pt startPt) {
        final int width = dim.width, height = dim.height;
        final int[] remainDiffInts = remainDiffData.getData();
        int x,y,idx; 
        y = startPt.y;
        // find on first (partial) line
        for(x = startPt.x,idx = y * width + x; x < width; x++,idx++) {
            if (remainDiffInts[idx] != 0) {
                res.x = x;
                res.y = y;
                return true;
            }
        }
        // find on remaining lines
        for(y = startPt.y + 1,idx = y * width + x; y < height; y++) {
            for(x = 0,idx = y * width + x; x < width; x++,idx++) {
                if (remainDiffInts[idx] != 0) {
                    res.x = x;
                    res.y = y;
                    return true;
                }
            }
        }
        // not found
        res.x = -1;
        res.y = -1;
        return false;
    }

    
    private void dilateRectUntilNoMoreDiff(Rect rect) {
        final int width = dim.width, height = dim.height;
        final int[] remainDiffInts = remainDiffData.getData();
        QuadDirection dir = QuadDirection.RIGHT;
        int lastDiffRight = 1;
        int lastDiffLeft = 1;
        int lastDiffDown = 1;
        int x,y,idx;
        loop_dir: for(;; dir = dir.nextCyclicRightLeftDownDirection()) {
            // test enlarge rect in direction 'dir'
            switch(dir) {
            case RIGHT:
                if (rect.toX + 1 < width) {
                    x = rect.toX + 1;
                    y = rect.fromY;
                    idx = y*width+x;
                    int minX = rect.toX + 1;
                    int maxX = Math.min(width, minX + 1 + dilateRect);
                    for(; y < rect.toY; y++,idx=y*width+x) { // TODO optim idx+=?
                        idx += rect.toX + 1 - x;
                        x = rect.toX + 1;
                        // idx=y*width+x; // TODO
                        if (CHECK_IDX) ImageRasterUtils.checkIdx(idx, x, y, width);
                        for(x = rect.toX + 1; x < maxX; x++,idx++) {
                            if (remainDiffInts[idx] != 0) {
                                x++;
                                idx++;
                                rect.toX = x;
                                // optim: continue search right for same x,y
                                if (rect.toX + 1 < width) {
                                    minX = rect.toX + 1;
                                    maxX = Math.min(width, minX + 1 + dilateRect);
                                    lastDiffRight = 1; // should restart from minX ..
                                } else {
                                    lastDiffRight = 1;
                                    continue loop_dir;
                                }
                            }
                        }
                    }
                }
                lastDiffRight = 0;
                if (lastDiffLeft == 0 && lastDiffDown == 0) {
                    return;
                }
                break;
            case LEFT:
                if (rect.fromX - 1 >= 0) {
                    int minX = Math.max(0, rect.fromX - 1 - dilateRect);
                    int maxX = rect.fromX;
                    x = minX;
                    y = rect.fromY;
                    idx=y*width+x;
                    for(; y < rect.toY; y++,idx+=width) {
                        idx += maxX - x;
                        x = maxX;
                        // idx=y*width+x; // TODO
                        if (CHECK_IDX) ImageRasterUtils.checkIdx(idx, x, y, width);
                        for(; x >= minX; x--,idx--) {
                            if (remainDiffInts[idx] != 0) {
                                // optim: continue search left for same x,y
                                rect.fromX = x-1; 
                                if (rect.fromX - 1 >= 0) {
                                    minX = Math.max(0, rect.fromX - 1 - dilateRect);
                                    maxX = rect.fromX;
                                    lastDiffRight = 1; // should restart from minX ..
                                } else {
                                    lastDiffLeft = 1;
                                    continue loop_dir;
                                }
                            }
                        }
                    }
                }
                lastDiffLeft = 0;
                if (lastDiffRight == 0  && lastDiffDown == 0) {
                    return;
                }
                break;
            case DOWN:
                if (rect.toY + 1 < height) {
                    x = rect.fromX;
                    y = rect.toY + 1;
                    int maxY = Math.min(height,  y + 1 + dilateRect);
                    idx = y*width+x;
                    loop_y: for(; y < maxY; y++,idx+=width) {
                        idx += rect.fromX - x;
                        x = rect.fromX;
                        if (CHECK_IDX) ImageRasterUtils.checkIdx(idx, x, y, width);
                        for(x = rect.fromX; x < rect.toX; x++,idx++) {
                            if (remainDiffInts[idx] != 0) {
                                rect.toY = y + 1;
                                // optim: continue search left for same x,y
                                if (rect.toY + 1 < height) {
                                    maxY = Math.min(height,  y + 1 + dilateRect);
                                    lastDiffDown = 1;
                                    y++;
                                    idx += width;
                                    continue loop_y;
                                } else {
                                    lastDiffDown = 1;
                                    continue loop_dir;
                                }
                            }
                        }
                    }
                }
                lastDiffDown = 0;
                if (lastDiffRight == 0 && lastDiffLeft == 0) {
                    return;
                }
                break;
            default:
                break;
            }
        }
    }
    
    public Pt getFirstDiffPt() {
        return firstDiffPt;
    }
    
}
