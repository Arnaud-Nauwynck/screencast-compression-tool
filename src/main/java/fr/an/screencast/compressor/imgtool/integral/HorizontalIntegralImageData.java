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
    
    /**
     * find first x, where data[x][y] != 0
     * (using fast dichotomy search)
     * @return x-coord of first non 0 image pixel,   -1 if all pixels are 0
     */
    public int findFirstLinePt(int y, boolean optimAssumeBinImage) {
        final int idx_y = y*dim.width;
        int fromX = 0;
        int idx_from = idx_y + fromX; 
        if (data[idx_from] != 0) {
            return 0;
        }
        int toX = dim.width-1;
        int integral = integralHorizontalLine(fromX, y, toX);
        if (integral == 0) {
            return -1;
        } else {
            while(integral != 0) {
                if (optimAssumeBinImage) {
                    // OPTIM: assuming only 0 and 1 (binary image)
                    // img:  0 0 0 1 1 0 1...   0
                    // int=> 0 0 0 1 2 2 3 ..   N
                    //                          /\
                    //                      <-----   shift left of N-1
                    //                    /\
                    if (integral == 1) {
                        if (data[idx_y + toX] != 0) {
                            return toX;
                        }
                    } else {
                        toX -= integral-1;
                        integral = integralHorizontalLine(fromX, y, toX);
                    }
                }
                int mid = (fromX + toX) >>> 1;
                int integralLeft = integralHorizontalLine(fromX, y, mid);
                if (integralLeft == 0) {
                    fromX = mid + 1;
                    idx_from = idx_y + fromX;
                    if (data[idx_from] != 0) {
                        return fromX;
                    }
                    // assert integral == integralHorizontalLine(fromX, y, mid);
                } else { // integralLeft != 0
                    if (fromX == mid) {
                        return mid;
                    }
                    toX = mid;
                    integral = integralLeft;
                    // assert integral == integralHorizontalLine(fromX, y, mid);
                }
            }
            return -1; // should not occur
        }
    }
    
    
}
