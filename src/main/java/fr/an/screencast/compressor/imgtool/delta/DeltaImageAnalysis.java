package fr.an.screencast.compressor.imgtool.delta;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import fr.an.screencast.compressor.imgtool.integral.HorizontalIntegralImageData;
import fr.an.screencast.compressor.imgtool.integral.IntegralImageData;
import fr.an.screencast.compressor.imgtool.integral.VerticalIntegralImageData;
import fr.an.screencast.compressor.imgtool.utils.RasterImageFunction;
import fr.an.screencast.compressor.imgtool.utils.RasterImageFunctions;
import fr.an.screencast.compressor.utils.Dim;

public class DeltaImageAnalysis {

    private static final boolean DEBUG = true;
    
    private final Dim dim;
    
    private int[] prevData;
    private int[] data;
    
    private IntegralImageData diffCountIntegralImageData;
    private HorizontalIntegralImageData diffCountHorizontalIntegralImageData;
    private VerticalIntegralImageData diffCountVerticalIntegralImageData;
    
    private Rectangle diffRect = new Rectangle();
    private List<Rectangle> diffRects = new ArrayList<Rectangle>();
    
    private int rawFirstDiffPtx, firstDiffPty;
    private int firstDiffPtx;
    private int filterSize = 5;
    
    // ------------------------------------------------------------------------
    
    public DeltaImageAnalysis(Dim dim, int[] prevData, int[] data) {
        this.dim = dim;
        this.prevData = prevData;
        this.data = data;

        this.diffCountIntegralImageData = new IntegralImageData(dim); 
        this.diffCountHorizontalIntegralImageData = new HorizontalIntegralImageData(dim); 
        this.diffCountVerticalIntegralImageData = new VerticalIntegralImageData(dim); 
    }

    // ------------------------------------------------------------------------

    public void setData(int[] prevData, int[] data) {
        this.prevData = prevData;
        this.data = data;
    }
    
    public void computeDiff() {
        final int[] prevData = this.prevData;
        final int[] data = this.data;
        final int height = dim.height;
        
        RasterImageFunction binaryDiff = RasterImageFunctions.binaryDiff(dim, data, prevData);
        diffCountIntegralImageData.setComputeFrom(binaryDiff);
        diffCountHorizontalIntegralImageData.setComputeFrom(binaryDiff);
        diffCountVerticalIntegralImageData.setComputeFrom(binaryDiff);
        
        firstDiffPtx = -1;
        firstDiffPty = -1;
        rawFirstDiffPtx = -1;
        label_findFirstPt: for (int y = 0; y < height; y++) {
            int tmpx= diffCountHorizontalIntegralImageData.findFirstLinePt(y, true);
            if (tmpx != -1) {
                if (tmpx == 1919) {
                    diffCountHorizontalIntegralImageData.findFirstLinePt(y, true);
                    continue; // BUG ??  
                }
                rawFirstDiffPtx = tmpx;
                firstDiffPty = y;
                break label_findFirstPt;
            }
        }
        if (rawFirstDiffPtx != -1) {
            firstDiffPtx = rawFirstDiffPtx;
            int maxFilterY = Math.min(firstDiffPty+1+filterSize, height); 
            for (int y = firstDiffPty+1; y < maxFilterY; y++) {
                int tmpx = diffCountHorizontalIntegralImageData.findFirstLinePt(y, true);
                if (tmpx != -1) {
                    firstDiffPtx = Math.min(firstDiffPtx, tmpx);
                }
            }
        }
        
        
        if (firstDiffPtx == -1) {
            System.out.println("NO Diff");
        }
        // TODO ... 
    }
    
    public IntegralImageData getDiffCountIntegralImageData() {
        return diffCountIntegralImageData;
    }

    public HorizontalIntegralImageData getDiffCountHorizontalIntegralImageData() {
        return diffCountHorizontalIntegralImageData;
    }

    public VerticalIntegralImageData getDiffCountVerticalIntegralImageData() {
        return diffCountVerticalIntegralImageData;
    }

    public List<Rectangle> getDiffRects() {
        return diffRects;
    }

    public int getRawFirstDiffPtx() {
        return rawFirstDiffPtx;
    }
    public int getFirstDiffPtx() {
        return firstDiffPtx;
    }
    public int getFirstDiffPty() {
        return firstDiffPty;
    }


    
    
}
