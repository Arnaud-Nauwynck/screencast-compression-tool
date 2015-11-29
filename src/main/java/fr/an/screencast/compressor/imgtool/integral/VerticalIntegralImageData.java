package fr.an.screencast.compressor.imgtool.integral;

import fr.an.screencast.compressor.imgtool.utils.ImageData;
import fr.an.screencast.compressor.imgtool.utils.RasterImageFunction;
import fr.an.screencast.compressor.utils.Dim;

/**
 * helper class for storing an integral image data (dim, int[]), 
 * containing partial sum per vertical lines
 * 
 *
 */
public class VerticalIntegralImageData extends ImageData {

    // ------------------------------------------------------------------------
    
    public VerticalIntegralImageData(Dim dim, int[] data) {
        super(dim, data);
    }

    public VerticalIntegralImageData(Dim dim) {
        super(dim, new int[dim.width * dim.height]);
    }

    // ------------------------------------------------------------------------

    public void setComputeFrom(RasterImageFunction src) {
        final int width = dim.width, height = dim.height;
        final int[] data = this.data;
        for(int x = 0; x < width; x++) {
            int colSum = 0;
            for(int y = 0, idx_xy = x; y < height; y++,idx_xy+=width) {
                int val = src.eval(x, y, idx_xy);
                colSum += val;
                data[idx_xy] = colSum;
            }
        }
    }
    

    /**
     * @return integral on area {x} x [fromY,toY]
     */
    public int integralVerticalLine(int x, int fromY, int toY) {
        int fromYex = fromY-1;
        int intDown = safeGetAt(x, toY);
        int intUp   = safeGetAt(x, fromYex);
        return intDown - intUp;
    }
    
}
