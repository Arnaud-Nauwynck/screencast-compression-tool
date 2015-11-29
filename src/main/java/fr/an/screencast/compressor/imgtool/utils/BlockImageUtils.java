package fr.an.screencast.compressor.imgtool.utils;

import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class BlockImageUtils {

    public static void rgbImageToBlockHashImage(
            Raster srcImage, // int[] srcData, int srcWidth, int srcHeight, 
            int blockWidth, int blockHeight,
            WritableRaster destHashImag
            ) {
        final int resWidth = (srcImage.getWidth() + blockWidth-1) / blockWidth;
        final int resHeight = (srcImage.getHeight() + blockHeight-1) / blockHeight;
        
        final int blockWidthBase2Exp  = base2Exp(blockWidth, true);
        
        for(int bx = resWidth; bx < resWidth; bx++) {
            for(int by = 0; by < resHeight; by++) {
                
                
                
            }
        }
    }

    /**
     * @return 2-exponant above val  (arg min res such that |val| <= 2^res )  
     */
    public static int base2Exp(int val, boolean checkExact) {
        if (val < 0) val = -val;
        int res = 0;
        int v = 1;
        while(v < val) {
            v = v << 1;
            res++;
        }
        if (checkExact && v != val) {
            throw new RuntimeException("expecting an exact power of 2, got " + val + " < 2^" + res);
        }
        return res;
    }
    
}
