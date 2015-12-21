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
    
    public static final boolean CHECK = true;
    
    public static void checkIdx(int idx, int x, int y, int width) {
        if (CHECK) {
            int checkIdx = y * width + x;
            if (idx != checkIdx) {
                throw new IllegalStateException();
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
     * copy sub rect "Region Of Interest" from src image to corrsponding dest image with same the dimension 
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
    public static void drawRectImg(Dim destDim, final int[] dest, Dim srcDim, final int[] src, Pt destLocation) {
        final int width = destDim.getWidth();
        final int srcWidth = srcDim.getWidth();
        int srcIdx = 0;
        int idx = destLocation.x;
        final int lastY = Math.min(destDim.height, destLocation.y + srcDim.height);
        for(int y = destLocation.y; y < lastY; y++,idx+=width,srcIdx+=srcWidth) {
            System.arraycopy(dest, idx, src, srcIdx, srcWidth);
        }
    }
}
