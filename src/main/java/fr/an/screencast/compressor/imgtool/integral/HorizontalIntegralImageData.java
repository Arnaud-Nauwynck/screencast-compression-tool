package fr.an.screencast.compressor.imgtool.integral;

import fr.an.screencast.compressor.imgtool.utils.ImageData;
import fr.an.screencast.compressor.imgtool.utils.RasterImageFunction;
import fr.an.screencast.compressor.utils.Dim;

/**
 * helper class for storing an integral image data (dim, int[]), 
 * containing partial sum per horizontal lines
 * 
 *
 */
public class HorizontalIntegralImageData extends ImageData {

    // ------------------------------------------------------------------------
    
    public HorizontalIntegralImageData(Dim dim, int[] data) {
        super(dim, data);
    }

    public HorizontalIntegralImageData(Dim dim) {
        super(dim, new int[dim.width * dim.height]);
    }

    // ------------------------------------------------------------------------

    public void setComputeFrom(RasterImageFunction src) {
        final int width = dim.width, height = dim.height;
        final int[] data = this.data;
        for(int y = 0, idx_xy = 0; y < height; y++) {
            int rowSum = 0;
            for(int x = 0; x < width; x++,idx_xy++) {
                int val = src.eval(x, y, idx_xy);
                rowSum += val;
                data[idx_xy] = rowSum;
            }
        }
    }
    
    /**
     * @return integral on area [fromX,toX] x {y}
     */
    public int integralHorizontalLine(int fromX, int fromY, int toX) {
        int fromXex = fromX-1;
        int intRight = safeGetAt(toX,       fromY);
        int intLeft  = safeGetAt(fromXex,   fromY);
        return intRight - intLeft;
    }
    
}
