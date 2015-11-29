package fr.an.screencast.compressor;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.ImageRasterUtils;

public class DeltaImageAnalysis {

    private static final boolean DEBUG = true;
    private static final boolean DEBUG_DIFF_LINE = 
            true;
//        false;
    
    private final Dim dim;
    
    private int[] prevData;
    private int[] data;
    
    private int[] diffCountIntegral;
    
    private Rectangle diffRect = new Rectangle();  
    private List<Rectangle> diffRects = new ArrayList<Rectangle>();
    
    // ------------------------------------------------------------------------
    
    public DeltaImageAnalysis(Dim dim, int[] prevData, int[] data) {
        this.dim = dim;
        this.prevData = prevData;
        this.data = data;

        this.diffCountIntegral = new int[dim.width * dim.height]; 
    }

    // ------------------------------------------------------------------------

    public void setData(int[] prevData, int[] data) {
        this.prevData = prevData;
        this.data = data;
    }
    
    public void computeDiff() {
        final int[] prevData = this.prevData;
        final int[] data = this.data;
        
        ImageRasterUtils.computeDiffImageIntegral(diffCountIntegral, dim, data, prevData);
        
        // TODO ... 
    }
    

    public int[] getDiffCountIntegral() {
        return diffCountIntegral;
    }

    public List<Rectangle> getDiffRects() {
        return diffRects;
    }
        
}
