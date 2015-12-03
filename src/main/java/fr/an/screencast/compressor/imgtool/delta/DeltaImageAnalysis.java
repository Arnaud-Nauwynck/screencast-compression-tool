package fr.an.screencast.compressor.imgtool.delta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.an.screencast.compressor.imgtool.integral.HorizontalIntegralImageData;
import fr.an.screencast.compressor.imgtool.integral.VerticalIntegralImageData;
import fr.an.screencast.compressor.imgtool.utils.ImageData;
import fr.an.screencast.compressor.imgtool.utils.RasterImageFunction;
import fr.an.screencast.compressor.imgtool.utils.RasterImageFunctions;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.QuadDirection;
import fr.an.screencast.compressor.utils.Rect;

public class DeltaImageAnalysis implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;

    private static final boolean DEBUG_CHECK = false;
    
    private int filterSize = 5;
    private int dilateRect2X = 5;
    private int dilateRectY = 10;

    
    private final Dim dim;
    
    private int[] prevData;
    private int[] data;
    private ImageData remainDiffData;

    private HorizontalIntegralImageData diffHorizontalIntegralImageData;
    private VerticalIntegralImageData diffVerticalIntegralImageData;

    private HorizontalIntegralImageData checkDiffHorizontalIntegralImageData;
    private VerticalIntegralImageData checkDiffVerticalIntegralImageData;
    private HorizontalIntegralImageData checkBeforeDiffHorizontalIntegralImageData;
    private VerticalIntegralImageData checkBeforeDiffVerticalIntegralImageData;

    // for debug display only
    private HorizontalIntegralImageData initDiffHorizontalIntegralImageData;
    private VerticalIntegralImageData initDiffVerticalIntegralImageData;

    private List<Rect> diffRects = new ArrayList<Rect>();
    
    private Pt rawFirstDiffPt = new Pt();
    private Pt firstDiffPt = new Pt();
    
    // ------------------------------------------------------------------------
    
    public DeltaImageAnalysis(Dim dim, int[] prevData, int[] data) {
        this.dim = dim;
        this.prevData = prevData;
        this.data = data;
        this.remainDiffData = new ImageData(dim);
        
        this.initDiffHorizontalIntegralImageData = new HorizontalIntegralImageData(dim); 
        this.initDiffVerticalIntegralImageData = new VerticalIntegralImageData(dim); 
        if (DEBUG_CHECK) {
            checkDiffHorizontalIntegralImageData = new HorizontalIntegralImageData(dim);
            checkDiffVerticalIntegralImageData = new VerticalIntegralImageData(dim);
            checkBeforeDiffHorizontalIntegralImageData = new HorizontalIntegralImageData(dim);
            checkBeforeDiffVerticalIntegralImageData = new VerticalIntegralImageData(dim);
        }            
            
        this.diffHorizontalIntegralImageData = new HorizontalIntegralImageData(dim); 
        this.diffVerticalIntegralImageData = new VerticalIntegralImageData(dim); 
    }

    // ------------------------------------------------------------------------

    public void setData(int[] prevData, int[] data) {
        this.prevData = prevData;
        this.data = data;
    }
    
    public void computeDiff() {
        final int[] prevData = this.prevData;
        final int[] data = this.data;
        
        RasterImageFunction binaryDiff = RasterImageFunctions.binaryDiff(dim, data, prevData);
        remainDiffData.set(binaryDiff);
        
        diffHorizontalIntegralImageData.setComputeFrom(binaryDiff);
        diffVerticalIntegralImageData.setComputeFrom(binaryDiff);

        initDiffHorizontalIntegralImageData.setCopyData(diffHorizontalIntegralImageData);
        initDiffVerticalIntegralImageData.setCopyData(diffVerticalIntegralImageData);

        diffRects.clear();

        int findFirstDiffY = 0;
        findFirstDiffPt(firstDiffPt, rawFirstDiffPt, findFirstDiffY, filterSize);
        if (firstDiffPt.x == -1) {
            // System.out.println("NO Diff");
            return; 
        }

        for(;;) {
            Pt diffPt = new Pt();
            Pt rawDiffPt = new Pt();
            findFirstDiffPt(diffPt, rawDiffPt, findFirstDiffY, filterSize);
    
            if (diffPt.x == -1) {
                break; 
            }
                        
            // starting from diff point.. try increase rectange on left,down,right until enclosed by non-diff area 
            // (= until diff integral is locally constant)
            Rect rect = new Rect(diffPt, diffPt);
            // initial dilatation (cf filterSize while searching for stable firstDiffPt)
            // rect.fromX = Math.min(rect.fromX, rawFirstDiffPt.x);
            rect.fromX = Math.min(rect.fromX, rawDiffPt.x);
            rect.toX = Math.max(rect.toX, rawDiffPt.x);
    
            // dilatation...
            rect.fromX = Math.max(0, rect.fromX - dilateRect2X);
            rect.toX = Math.min(dim.width-1, rect.toX + dilateRect2X);
            rect.toY = Math.min(dim.height, rect.toY + dilateRectY);
    
            int checkInt = diffHorizontalIntegralImageData.integralHorizontalLine(rect.fromX, diffPt.y, rect.toX);
            if (checkInt == 0) {
                // TODO BUG .. should not occur
                System.out.println("TODO ERROR ... skip diff detection");
                break;
            }
            
            dilateRectUntilNoMoreDiff(rect);
            
            // clear diff in rectangle: update remaining integralImage
            if (DEBUG_CHECK) {
                checkBeforeDiffHorizontalIntegralImageData.setCopyData(diffHorizontalIntegralImageData);
                checkBeforeDiffVerticalIntegralImageData.setCopyData(diffVerticalIntegralImageData);
            }
            
            
            diffHorizontalIntegralImageData.updateComputeClearRect(rect);
            diffVerticalIntegralImageData.updateComputeClearRect(rect);
            
            remainDiffData.setFillRect(rect, 0);

            diffRects.add(rect);
            findFirstDiffY = diffPt.y;


            // check integral...
            if (DEBUG_CHECK) {
                checkDiffHorizontalIntegralImageData.setComputeFrom(remainDiffData);
                checkDiffVerticalIntegralImageData.setComputeFrom(remainDiffData);
                
                try {
                    checkDiffHorizontalIntegralImageData.checkEquals(diffHorizontalIntegralImageData);
                    checkDiffVerticalIntegralImageData.checkEquals(diffVerticalIntegralImageData);
                } catch(Exception ex) {
                    // redo
                    checkBeforeDiffHorizontalIntegralImageData.updateComputeClearRect(rect);
                    checkBeforeDiffVerticalIntegralImageData.updateComputeClearRect(rect);
                }   
            }
            
        }
        
    }


    private void findFirstDiffPt(Pt res, Pt optRawResPt, int startY, int filterSize) {
        final int height = dim.height;
        int rawFirstX = -1;
        res.x = -1;
        res.y = -1;
        label_findFirstPt: for (int y = startY; y < height; y++) {
            int tmpx= diffHorizontalIntegralImageData.findFirstLinePt(y, true);
            if (tmpx != -1) {
                res.x = tmpx;
                res.y = y;
                rawFirstX = tmpx;
                if (optRawResPt != null) {
                    optRawResPt.x = tmpx; 
                    optRawResPt.y = y;
                }
                break label_findFirstPt;
            }
        }
        if (rawFirstX != -1) {
            // filter best min "x" using few more lines
            int filterMinX = rawFirstX;
            final int maxFilterY = Math.min(res.y+1+filterSize, height); 
            for (int y = res.y+1; y < maxFilterY; y++) {
                int tmpx = diffHorizontalIntegralImageData.findFirstLinePt(y, true);
                if (tmpx != -1) {
                    filterMinX = Math.min(filterMinX, tmpx);
                }
            }
            res.x = filterMinX;
        }
    }

    
    private void dilateRectUntilNoMoreDiff(Rect rect) {
        QuadDirection dir = QuadDirection.RIGHT;
        
        int lastDiffRight = 1;
        int lastDiffLeft = 1;
        int lastDiffDown = 1;
        for(;; dir = dir.nextCyclicRightLeftDownDirection()) {
            // test enlarge rect in dir
            int lastDiff = 0;
            switch(dir) {
            case RIGHT:
                if (rect.toX + 1 < dim.width) {
                    lastDiffRight = diffVerticalIntegralImageData.integralVerticalLine(
                        rect.toX+1, rect.fromY, rect.toY);
                    if (lastDiffRight != 0) {
                        rect.toX++;
                    } 
                } else {
                    lastDiffRight = 0;
                }
                lastDiff = lastDiffRight;
                break;
            case LEFT:
                if (rect.fromX - 1 >= 0) {
                    lastDiffLeft = diffVerticalIntegralImageData.integralVerticalLine(
                        rect.fromX-1, rect.fromY, rect.toY);
                    if (lastDiffLeft != 0) {
                        rect.fromX--;
                    } 
                } else {
                    lastDiffLeft = 0;
                }
                lastDiff = lastDiffRight;
                break;
            case DOWN:
                if (rect.toY + 1 < dim.height) {
                    lastDiffDown = diffHorizontalIntegralImageData.integralHorizontalLine(
                        rect.fromX, rect.toY+1, rect.toX);
                    if (lastDiffDown != 0) {
                        rect.toY++;
                    } 
                } else {
                    lastDiffDown = 0;
                }
                lastDiff = lastDiffDown;
                break;
            default:
                break;
            }
            
            if (lastDiff == 0) {
                if (lastDiffRight == 0 && lastDiffLeft == 0 && lastDiffDown == 0) {
                    break;
                }
            }
        }
    }
    
    

    public HorizontalIntegralImageData getDiffHorizontalIntegralImageData() {
        return diffHorizontalIntegralImageData;
    }

    public VerticalIntegralImageData getDiffVerticalIntegralImageData() {
        return diffVerticalIntegralImageData;
    }

    public List<Rect> getDiffRects() {
        return diffRects;
    }

    public Pt getRawFirstDiffPt() {
        return rawFirstDiffPt;
    }
    public Pt getFirstDiffPt() {
        return firstDiffPt;
    }
    
}
