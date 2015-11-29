package fr.an.screencast.compressor.imgtool.utils;

public final class RGBUtils {

    // cf new Color()
    public static int rgb2Int(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8)  |
                ((b & 0xFF) << 0);
    }
    
    public static int rgb2Int256(int r, int g, int b, int a) {
        return rgb2Int(toByte256(r), toByte256(g), toByte256(b), toByte256(a));
    }
    
    public static int toByte256(int val) {
        if (val <= 0) return 0;
        else if (val < 256) return val;
        else return 255;
    }

}
