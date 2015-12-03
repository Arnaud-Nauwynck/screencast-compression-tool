package fr.an.screencast.compressor.imgtool.integral;

import fr.an.screencast.compressor.imgtool.utils.ImageData;
import fr.an.screencast.compressor.imgtool.utils.RasterImageFunction;
import fr.an.screencast.compressor.utils.Dim;

public class IntegralImageData extends ImageData {

    /** */
    private static final long serialVersionUID = 1L;

    // ------------------------------------------------------------------------
    
    public IntegralImageData(Dim dim, int[] data) {
        super(dim, data);
    }

    public IntegralImageData(Dim dim) {
        super(dim);
    }

    // ------------------------------------------------------------------------

    /**
     * compute integral image for src
     */
    public void setComputeFrom(RasterImageFunction src) {
        final int width = dim.width, height = dim.height;
        final int[] data = this.data;
        // first row
        int rowSum = 0;
        for(int x = 0; x < width; x++) {
            int diff = src.eval(x, 0, x);
            rowSum += diff;
            data[x] = rowSum;
        }        
        // following rows 
        for(int y = 1, idx_up_xy=0,idx_xy = 1*width; y < height; y++) {
            rowSum = 0;
            for(int x = 0; x < width; x++,idx_up_xy++,idx_xy++) {
                int val = src.eval(x, y, idx_xy);
                rowSum += val;
                data[idx_xy] = data[idx_up_xy] + rowSum;
            }
        }
    }
    
    /**
     * @return integral on area [fromX,toX] x [fromY,toY]
     */
    public int integralPt2PtInclude(int fromX, int fromY, int toX, int toY) {
        int fromXex = fromX-1;
        int fromYex = fromY-1;
        int intDownRight = safeGetAt(toX,       toY);
        int intDownLeft  = safeGetAt(fromXex,   toY);
        int intUpRight   = safeGetAt(toX,       fromYex);
        int intUpLeft    = safeGetAt(fromXex,   fromYex);
        
        return intDownRight - intDownLeft - intUpRight + intUpLeft;
    }
    
}
