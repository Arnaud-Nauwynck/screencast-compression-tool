package fr.an.screencast.compressor.imgtool.utils;

import fr.an.screencast.compressor.utils.Rect;

public final class MorphologicImgRasterUtils {

    /* private to force all static */
    private MorphologicImgRasterUtils() {}
    
    public static void erodeMinRGB(final int[] dest, final int[] src, final int width, final int height, final Rect roi) { 
        // assume roi is strictly within image dimension (copy border otherwise)
        // ImageRasterUtils.copyRect(dest, src, width, height, roi);
    
        final int roiFromX = Math.max(1, roi.fromX);
        final int roiFromY = Math.max(1, roi.fromY);
        final int roiToX = Math.min(width-1, roi.toX);
        final int roiToY = Math.min(height-1, roi.toY);
        final int incrIdxY = width + roiFromX - roiToX;
    
        int x, y, idx;
        
        x = roiFromX;
        y = roiFromY;
        idx = y*width+x;
        for (y = roiFromY; y < roiToY; y++,idx+=incrIdxY) {
            x = roiFromX;            
            ImageRasterUtils.checkIdx(idx, x, y, width); 
            // left column: x-1
            int rgb_xm1_ym1 = src[idx-1-width];
            int rgb_xm1_y   = src[idx-1];
            int rgb_xm1_yp1 = src[idx-1+width];
            int min_r_xm1 = RGBUtils.minRed  (rgb_xm1_ym1, rgb_xm1_y, rgb_xm1_yp1); 
            int min_g_xm1 = RGBUtils.minGreen(rgb_xm1_ym1, rgb_xm1_y, rgb_xm1_yp1);
            int min_b_xm1 = RGBUtils.minBlue (rgb_xm1_ym1, rgb_xm1_y, rgb_xm1_yp1);
            
            // curr column: x
            int rgb_x_ym1 = src[idx-width];
            int rgb_x_y   = src[idx];
            int rgb_x_yp1 = src[idx+width];
            int min_r_x = RGBUtils.minRed  (rgb_x_ym1, rgb_x_y, rgb_x_yp1); 
            int min_g_x = RGBUtils.minGreen(rgb_x_ym1, rgb_x_y, rgb_x_yp1);
            int min_b_x = RGBUtils.minBlue (rgb_x_ym1, rgb_x_y, rgb_x_yp1);
            
            for (x = roiFromX; x < roiToX; x++,idx++) {
                int rgb_xp1_ym1 = src[idx+1-width];
                int rgb_xp1_y   = src[idx+1];
                int rgb_xp1_yp1 = src[idx+1+width];
                int min_r_xp1 = RGBUtils.minRed  (rgb_xp1_ym1, rgb_xp1_y, rgb_xp1_yp1); 
                int min_g_xp1 = RGBUtils.minGreen(rgb_xp1_ym1, rgb_xp1_y, rgb_xp1_yp1);
                int min_b_xp1 = RGBUtils.minBlue (rgb_xp1_ym1, rgb_xp1_y, rgb_xp1_yp1);

                int res_r = RGBUtils.min(min_r_xm1, min_r_x, min_r_xp1);
                int res_g = RGBUtils.min(min_g_xm1, min_g_x, min_g_xp1);
                int res_b = RGBUtils.min(min_b_xm1, min_b_x, min_b_xp1);
                
                dest[idx] = RGBUtils.rgb2Int(res_r, res_g, res_b);
                
                // shift columns x-1 <- x <- x+1
                min_r_xm1 = min_r_x; min_r_x = min_r_xp1;
                min_g_xm1 = min_g_x; min_g_x = min_g_xp1;
                min_b_xm1 = min_b_x; min_b_x = min_b_xp1;
            }
        }
    }

    
    public static void dilateMaxRGB(final int[] dest, final int[] src, final int width, final int height, final Rect roi) { 
        // assume roi is strictly within image dimension (copy border otherwise)
        // ImageRasterUtils.copyRect(destData, srcData, width, height, roi);
    
        final int roiFromX = Math.max(1, roi.fromX);
        final int roiFromY = Math.max(1, roi.fromY);
        final int roiToX = Math.min(width-1, roi.toX);
        final int roiToY = Math.min(height-1, roi.toY);
        final int incrIdxY = width + roiFromX - roiToX;
    
        int x, y, idx;
        x = roiFromX;
        y = roiFromY;
        idx = y*width+x;
        for (y = roiFromY; y < roiToY; y++,idx+=incrIdxY) {
            x = roiFromX;
            ImageRasterUtils.checkIdx(idx, x, y, width); 
            // left column: x-1
            int rgb_xm1_ym1 = src[idx-1-width];
            int rgb_xm1_y   = src[idx-1];
            int rgb_xm1_yp1 = src[idx-1+width];
            int max_r_xm1 = RGBUtils.maxRed  (rgb_xm1_ym1, rgb_xm1_y, rgb_xm1_yp1); 
            int max_g_xm1 = RGBUtils.maxGreen(rgb_xm1_ym1, rgb_xm1_y, rgb_xm1_yp1);
            int max_b_xm1 = RGBUtils.maxBlue (rgb_xm1_ym1, rgb_xm1_y, rgb_xm1_yp1);
            
            // curr column: x
            int rgb_x_ym1 = src[idx-width];
            int rgb_x_y   = src[idx];
            int rgb_x_yp1 = src[idx+width];
            int max_r_x = RGBUtils.maxRed  (rgb_x_ym1, rgb_x_y, rgb_x_yp1); 
            int max_g_x = RGBUtils.maxGreen(rgb_x_ym1, rgb_x_y, rgb_x_yp1);
            int max_b_x = RGBUtils.maxBlue (rgb_x_ym1, rgb_x_y, rgb_x_yp1);
            
            for (x = roiFromX; x < roiToX; x++,idx++) {
                int rgb_xp1_ym1 = src[idx+1-width];
                int rgb_xp1_y   = src[idx+1];
                int rgb_xp1_yp1 = src[idx+1+width];
                int max_r_xp1 = RGBUtils.maxRed  (rgb_xp1_ym1, rgb_xp1_y, rgb_xp1_yp1); 
                int max_g_xp1 = RGBUtils.maxGreen(rgb_xp1_ym1, rgb_xp1_y, rgb_xp1_yp1);
                int max_b_xp1 = RGBUtils.maxBlue (rgb_xp1_ym1, rgb_xp1_y, rgb_xp1_yp1);

                int res_r = RGBUtils.max(max_r_xm1, max_r_x, max_r_xp1);
                int res_g = RGBUtils.max(max_g_xm1, max_g_x, max_g_xp1);
                int res_b = RGBUtils.max(max_b_xm1, max_b_x, max_b_xp1);
                
                dest[idx] = RGBUtils.rgb2Int(res_r, res_g, res_b);
                
                // shift columns x-1 <- x <- x+1
                max_r_xm1 = max_r_x; max_r_x = max_r_xp1;
                max_g_xm1 = max_g_x; max_g_x = max_g_xp1;
                max_b_xm1 = max_b_x; max_b_x = max_b_xp1;
            }
        }
    }

    
}
