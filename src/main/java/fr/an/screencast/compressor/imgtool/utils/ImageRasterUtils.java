package fr.an.screencast.compressor.imgtool.utils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import fr.an.screencast.compressor.utils.Dim;

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
}
