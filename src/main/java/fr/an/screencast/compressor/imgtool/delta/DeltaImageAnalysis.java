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
        
        RasterImageFunction binaryDiff = RasterImageFunctions.binaryDiff(dim, data, prevData);
        diffCountIntegralImageData.setComputeFrom(binaryDiff);
        diffCountHorizontalIntegralImageData.setComputeFrom(binaryDiff);
        diffCountVerticalIntegralImageData.setComputeFrom(binaryDiff);
        
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
        
}
