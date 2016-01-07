package fr.an.screencast.compressor.imgtool.utils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.Rect;

public class ImageRasterUtils {

    public static int[] toInts(BufferedImage img) {
        return ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
    }
    
    public static void copyData(BufferedImage dest, int[] src) {
        int[] destData = toInts(dest);
        System.arraycopy(src, 0, destData, 0, src.length);
    }

    public static final boolean CHECK = true;
    
    public static void checkIdx(int idx, int x, int y, int width) {
        if (CHECK) {
            int checkIdx = y * width + x;
            if (idx != checkIdx) {
                throw new IllegalStateException("expecting idx(=y*w+x) " + checkIdx + ", got " + idx + " for x:" + x + ", y:" + y + ", width:" + width);
            }
        }
    }
    
    public static void copyRectBorder(int[] dest, int[] src, int width, int height, Rect roi) {
        int y = roi.fromY;
        int idx = roi.fromY*width+roi.fromX;
        for (int x = roi.fromX; x < roi.toX; x++,idx++) {
            dest[idx] = src[idx]; 
        }
        y = roi.fromY+1;
        int idxFromX = y*width+roi.fromX, idxToX = y*width+roi.toX; 
        for (; y < roi.toY-1; y++,idxFromX+=width,idxToX+=width) {
            dest[idxFromX] = src[idxFromX];
            dest[idxToX] = src[idxToX];
        }
        y = roi.toY;
        idx = roi.toY*width+roi.fromX;
        for (int x = roi.fromX; x < roi.toX; x++,idx++) {
            dest[idx] = src[idx]; 
        }
    }

    /**
     * copy sub rect "Region Of Interest" from src image to corresponding dest image with same the dimension 
     * @param dest
     * @param src
     * @param width
     * @param height
     * @param roi
     */
    public static void copyRect(int[] dest, int[] src, int width, int height, Rect roi) {
        int y = roi.fromY, x = roi.fromX;
        int idx = y*width+x;
        final int roiWidth = roi.toX - roi.fromX;
        final int incrIdxY = width + roi.fromX - roi.toX; 
        for (y = roi.fromY; y < roi.toY; y++,idx+=incrIdxY) {
            System.arraycopy(dest, idx, src, idx, roiWidth);
        }
    }

    /**
     * copy from src image to dest image sub rectangle (not the same dimension) 
     * @param destDim
     * @param dest
     * @param srcDim
     * @param src
     * @param destLocation
     */
    public static void drawRectImg(Dim destDim, final int[] dest, Pt destLocation, 
            Dim srcDim, final int[] src, Rect srcROI
            ) {
        final int destWidth = destDim.getWidth();
        final int srcWidth = srcDim.getWidth();
        final int copyHeight = Math.min(destDim.height - destLocation.y, 
                    Math.min(srcDim.height - srcROI.fromY, srcROI.getHeight()));
        final int copyWidth = Math.min(destDim.width - destLocation.x, 
                    Math.min(srcDim.width - srcROI.fromX, srcROI.getWidth()));
        int srcIdx = srcROI.fromY * srcDim.width + srcROI.fromX;
        int destIdx = destLocation.y * destWidth + destLocation.x;
        final int destMaxIdx = (destLocation.y + copyHeight) * destWidth + destLocation.x;
        for(; destIdx < destMaxIdx; destIdx+=destWidth,srcIdx+=srcWidth) {
            System.arraycopy(src, srcIdx, dest, destIdx, copyWidth);
        }
    }

    /**
     * copy from src image to dest image 
     */
    public static void drawRectImg(Dim destDim, final int[] dest, Rect rect, final int[] src) {
        final int destW = destDim.width;
        final int srcW = rect.getWidth();
        final int srcMaxIdx = src.length;
        int destIdx = rect.fromY * destW + rect.fromX;
        int srcIdx = 0;
        for(; srcIdx < srcMaxIdx; destIdx+=destW,srcIdx+=srcW) {
            System.arraycopy(src, srcIdx, dest, destIdx, srcW);
        }
    }

    public static int[] getCopyData(Dim srcDim, final int[] src, Rect srcROI) {
        final int W = srcDim.getWidth();
        final int destLen = srcROI.getArea();
        final int[] dest = new int[destLen];
        
        final int roiW = srcROI.getWidth();
        int srcIdx = srcROI.fromY * srcDim.width + srcROI.fromX;
        int destIdx = 0;
        for(; destIdx < destLen; destIdx+=roiW,srcIdx+=W) {
            System.arraycopy(src, srcIdx, dest, destIdx, roiW);
        }
        return dest;
    }
    
    
    public static void fillAlpha(int[] src) {
        final int len = src.length;
        int mask = RGBUtils.rgb2Int(0, 0, 0, 255);
        for(int i = 0; i < len; i++) {
            src[i] = src[i] | mask; // 0xFF000000;
        }
    }
}
