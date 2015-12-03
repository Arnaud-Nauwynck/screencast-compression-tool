package fr.an.screencast.compressor.imgtool.utils;

public final class RGBUtils {

    public static final int CMAX_255 = 255; 
    
    // cf new Color()
    public static int rgb2Int(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8)  |
                ((b & 0xFF) << 0);
    }

    public static int rgb2Int(int r, int g, int b) {
        return rgb2Int(r, g, b, 255);
    }
    public static int greyRgb2Int(int grey) {
        return rgb2Int(grey,  grey,  grey, 255);
    }
    
    public static int rgb2Int256(int r, int g, int b, int a) {
        return rgb2Int(toByte256(r), toByte256(g), toByte256(b), toByte256(a));
    }
    
    public static int toByte256(int val) {
        if (val <= 0) return 0;
        else if (val < 256) return val;
        else return 255;
    }

    
    public static int redOf(int value) {
        return (value >>> 16) & 0xFF;
    }
    
    public static int greenOf(int value) {
        return (value >>> 8) & 0xFF;
    }
    
    public static int blueOf(int value) {
        return value & 0xFF;
    }

    public static int alphaOf(int value) {
        return (value >>> 24) & 0xFF;
    }

    public static String toString(int rgb) {
        return redOf(rgb) + ";" + greenOf(rgb) + ";" + blueOf(rgb)
            + ((alphaOf(rgb) != 255)? ";" + alphaOf(rgb) : "");  
    }
}
