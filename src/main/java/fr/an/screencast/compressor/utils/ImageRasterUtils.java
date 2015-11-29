package fr.an.screencast.compressor.utils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class ImageRasterUtils {

    public static int[] toInts(BufferedImage img) {
        return ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
    }
    
    /**
     * compute integral image for differences count between 2 images
     * 
     * @param res
     * @param width
     * @param height
     * @param data
     * @param prevData
     */
    public static void computeDiffImageIntegral(final int[] res,
            final Dim dim, 
            final int[] data, final int[] prevData) {
        final int width = dim.width, height = dim.height;
        // first row
        int rowSum = 0;
        for(int x = 0; x < width; x++) {
            int diff = (data[x] != prevData[x])? 1 : 0;
            rowSum += diff;
            res[x] = rowSum;
        }        
        // following rows 
        for(int y = 1, idx_up_xy=0,idx_xy = 1*width; y < height; y++) {
            rowSum = 0;
            for(int x = 0; x < width; x++,idx_up_xy++,idx_xy++) {
                int diff = (data[idx_xy] != prevData[idx_xy])? 1 : 0;
                rowSum += diff;
                res[idx_xy] = res[idx_up_xy] + rowSum;
            }
        }
    }
    
    public static int integralGetForPtAndPtInclude(final Dim dim, final int[] integralImage, 
            int fromX, int fromY, int toX, int toY) {
        int fromXm1 = fromX-1;
        int fromYm1 = fromY-1;
        int intDownRight = safeGetAt(dim, integralImage, toX,       toY);
        int intDownLeft  = safeGetAt(dim, integralImage, fromXm1,   toY);
        int intUpRight   = safeGetAt(dim, integralImage, toX,       fromYm1);
        int intUpLeft    = safeGetAt(dim, integralImage, fromXm1,   fromYm1);
        
        return intDownRight - intDownLeft - intUpRight + intUpLeft;
    }
    
    public static int safeGetAt(Dim dim, final int[] image, int x, int y) {
        if (x < 0 || x >= dim.width || y < 0 || y >= dim.height) return 0;
        else return image[dim.width * y + x];
    }
}
