package fr.an.screencast.compressor.imgtool.integral;

import fr.an.screencast.compressor.imgtool.utils.ImageData;
import fr.an.screencast.compressor.imgtool.utils.RasterImageFunction;
import fr.an.screencast.compressor.imgtool.utils.RasterImageFunctions;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Rect;

/**
 * helper class for storing an integral image data (dim, int[]), 
 * containing partial sum per vertical lines
 * 
 *
 */
public class VerticalIntegralImageData extends ImageData {

    /** */
    private static final long serialVersionUID = 1L;

    // ------------------------------------------------------------------------
    
    public VerticalIntegralImageData(Dim dim, int[] data) {
        super(dim, data);
    }

    public VerticalIntegralImageData(Dim dim) {
        super(dim, new int[dim.width * dim.height]);
    }

    // ------------------------------------------------------------------------

    public void setComputeFrom(ImageData src) {
        setComputeFrom(RasterImageFunctions.of(src));
    }
    
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

    public void updateComputeClearRect(Rect rect) {
        final int width = dim.width, height = dim.height;
        final int[] data = this.data;
        for(int x = rect.fromX, idx_xy = rect.fromX; x <= rect.toX; x++) {
            int diffColSum = - integralVerticalLine(x, rect.fromY, rect.toY);
            if (diffColSum != 0) {
                idx_xy = index(x,rect.fromY);
                int intFromY = rect.fromY > 0? data[idx_xy-width] : 0;
                for(int y = rect.fromY; y <= rect.toY; y++,idx_xy+=width) {
                    data[idx_xy] = intFromY;
                }
                for(int y = rect.toY+1; y < height; y++,idx_xy+=width) {
                    data[idx_xy] += diffColSum;
                }
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
