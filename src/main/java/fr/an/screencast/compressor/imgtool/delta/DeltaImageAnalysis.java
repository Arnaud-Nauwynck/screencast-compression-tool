package fr.an.screencast.compressor.imgtool.delta;

import fr.an.screencast.compressor.dtos.delta.DeltaImageAnalysisResult;
import fr.an.screencast.compressor.dtos.delta.FrameDelta;
import fr.an.screencast.compressor.imgtool.utils.ImageData;
import fr.an.screencast.compressor.imgtool.utils.RasterImageFunction;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.QuadDirection;
import fr.an.screencast.compressor.utils.Rect;

public class DeltaImageAnalysis {

    private int minDilateRect2X = 8;
    private int minDilateRectY = 16;

    private int dilateRect2X = 4;
    private int dilateRectY = 4;
    
    private final Dim dim;
    
    private ImageData remainDiffData;

    private DeltaImageAnalysisResult deltaAnalysisResult;
    
    private Pt firstDiffPt = new Pt();
    
    // ------------------------------------------------------------------------
    
    public DeltaImageAnalysis(Dim dim, DeltaImageAnalysisResult deltaAnalysisResult) {
        this.dim = dim;
        this.deltaAnalysisResult = deltaAnalysisResult;
        this.remainDiffData = new ImageData(dim);
    }

    // ------------------------------------------------------------------------
    
    public FrameDelta computeDiff(int frameIndex, RasterImageFunction binaryDiff) {
        FrameDelta res = new FrameDelta(frameIndex);
        remainDiffData.set(binaryDiff);

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
            rect.fromX = Math.max(0, rect.fromX - minDilateRect2X);
            rect.toX = Math.min(dim.width, rect.toX + minDilateRect2X);
            rect.toY = Math.min(dim.height, rect.toY + minDilateRectY);
    
            // try increase rectange on left,down,right until enclosed by non-diff area
            dilateRectUntilNoMoreDiff(rect);
            
            remainDiffData.setFillRect(rect, 0);

            res.addFrameRectDelta(rect);
            
            findFromPt.y = diffPt.y;
            findFromPt.x = rect.toX;
            if (! findFromPt.setNextHorizontalScan(dim)) {
                break; // reached end of image
            }
        }
        
        if (res.getDeltas().isEmpty()) {
            // System.out.println("NO Diff");
            return null;
        }
        deltaAnalysisResult.addFrameDelta(res);
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
                    int minX = rect.toX + 1;
                    final int maxX = Math.min(width, minX + 1 + dilateRect2X);
                    for(y = rect.fromY, idx=y*width+minX; y < rect.toY; y++,idx=y*width+x) { // TODO optim idx+=?
                        for(x = rect.toX + 1; x < maxX; x++,idx++) {
                            if (remainDiffInts[idx] != 0) {
                                rect.toX = x+1; 
                                // rect.toX += Math.min(width, x+dilateRect2X);
                                lastDiffRight = 1;
                                continue loop_dir;
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
                    final int minX = Math.max(0, rect.fromX - 1 - dilateRect2X);
                    final int maxX = rect.fromX;
                    for(y = rect.fromY, idx=y*width+minX; y < rect.toY; y++,idx=y*width+x) { // TODO optim idx+=?
                        for(x = minX; x < maxX; x++,idx++) {
                            if (remainDiffInts[idx] != 0) {
                                rect.fromX = x; 
                                // rect.toX += Math.max(0, x-dilateRect2X);
                                lastDiffLeft = 1;
                                continue loop_dir;
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
                    y = rect.toY + 1;
                    final int maxY = Math.min(height,  y + 1 + dilateRectY);
                    idx = y*width+rect.fromX;
                    final int incIndexY = width - rect.toX + rect.fromX;
                    for(; y < maxY; y++,idx+=incIndexY) {
                        x = rect.fromX;
                        if (idx != y*width+x) throw new IllegalStateException();
                        for(x = rect.fromX; x < rect.toX; x++,idx++) {
                            if (remainDiffInts[idx] != 0) {
                                rect.toY = y+1;
                                // rect.toY += Math.min(height, y+dilateRectY);
                                lastDiffDown = 1;
                                continue loop_dir;
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
