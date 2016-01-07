package fr.an.screencast.compressor.imgtool.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import fr.an.util.bits.RuntimeIOException;

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
        return rgb2Int(r, g, b, 0);
    }
    public static int greyRgb2Int(int grey) {
        return rgb2Int(grey,  grey,  grey, 0);
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

    
    public static int minRGB(int rgb1, int rgb2, int rgb3) {
        int r = minRed(rgb1, rgb2, rgb3);
        int g = minGreen(rgb1, rgb2, rgb3);
        int b = minBlue(rgb1, rgb2, rgb3);
        int a = minAlpha(rgb1, rgb2, rgb3);
        return rgb2Int(r, g, b, a);
    }
    
    public static int maxRGB(int rgb1, int rgb2, int rgb3) {
        int r = maxRed(rgb1, rgb2, rgb3);
        int g = maxGreen(rgb1, rgb2, rgb3);
        int b = maxBlue(rgb1, rgb2, rgb3);
        int a = maxAlpha(rgb1, rgb2, rgb3);
        return rgb2Int(r, g, b, a);
    }
    
    
    public static int minRed(int rgb1, int rgb2, int rgb3) {
        int r1 = RGBUtils.redOf(rgb1), r2 = RGBUtils.redOf(rgb2), r3 = RGBUtils.redOf(rgb3);   
        return min(r1, r2, r3);
    }
    public static int minGreen(int rgb1, int rgb2, int rgb3) {
        int g1 = RGBUtils.greenOf(rgb1), g2 = RGBUtils.greenOf(rgb2), g3 = RGBUtils.greenOf(rgb3);   
        return min(g1, g2, g3);
    }
    public static int minBlue(int rgb1, int rgb2, int rgb3) {
        int b1 = RGBUtils.blueOf(rgb1), b2 = RGBUtils.blueOf(rgb2), b3 = RGBUtils.blueOf(rgb3);   
        return min(b1, b2, b3);
    }
    public static int minAlpha(int rgb1, int rgb2, int rgb3) {
        int b1 = RGBUtils.alphaOf(rgb1), b2 = RGBUtils.alphaOf(rgb2), b3 = RGBUtils.alphaOf(rgb3);   
        return min(b1, b2, b3);
    }

    public static int maxRed(int rgb1, int rgb2, int rgb3) {
        int r1 = RGBUtils.redOf(rgb1), r2 = RGBUtils.redOf(rgb2), r3 = RGBUtils.redOf(rgb3);   
        return max(r1, r2, r3);
    }
    public static int maxGreen(int rgb1, int rgb2, int rgb3) {
        int g1 = RGBUtils.greenOf(rgb1), g2 = RGBUtils.greenOf(rgb2), g3 = RGBUtils.greenOf(rgb3);   
        return max(g1, g2, g3);
    }
    public static int maxBlue(int rgb1, int rgb2, int rgb3) {
        int b1 = RGBUtils.blueOf(rgb1), b2 = RGBUtils.blueOf(rgb2), b3 = RGBUtils.blueOf(rgb3);   
        return max(b1, b2, b3);
    }
    public static int maxAlpha(int rgb1, int rgb2, int rgb3) {
        int b1 = RGBUtils.alphaOf(rgb1), b2 = RGBUtils.alphaOf(rgb2), b3 = RGBUtils.alphaOf(rgb3);   
        return max(b1, b2, b3);
    }

    public static int min(int a, int b, int c) {
        return Math.min(a, Math.min(b, c));
    }

    public static int max(int a, int b, int c) {
        return Math.max(a, Math.max(b, c));
    }

    public static byte[] intRGBsToGzipBytes(int[] imgData) {
        byte[] gzipBytes;
        try {
            ByteArrayOutputStream gzipBuffer = new ByteArrayOutputStream(imgData.length >> 1);
            DeflaterOutputStream gzipOut = new DeflaterOutputStream(gzipBuffer); 
            for(int i = 0; i < imgData.length; i++) {
                int rgba = imgData[i];
                int r = RGBUtils.redOf(rgba), g = RGBUtils.greenOf(rgba), b = RGBUtils.blueOf(rgba); 
                gzipOut.write(r);
                gzipOut.write(g);
                gzipOut.write(b);
            }
            // gzipOut.flush();
            gzipOut.finish();
            gzipOut.close();
            gzipBytes = gzipBuffer.toByteArray();
        } catch(IOException ex) {
            throw new RuntimeIOException("should not occur", ex);
        }
        return gzipBytes;
    }

    public static void gzipBytesToIntRGBs(int[] res, byte[] gzipBytes, int alpha) {
        ByteArrayInputStream gzipBuffer = new ByteArrayInputStream(gzipBytes);
        InflaterInputStream gzipIn = new InflaterInputStream(gzipBuffer); 
        int i = 0;
        try {
            while(i < res.length) {
                int r = gzipIn.read();
                int g = gzipIn.read();
                int b = gzipIn.read();
                res[i++] = RGBUtils.rgb2Int(r, g, b, alpha);
            }
        } catch(IOException ex) {
            throw new RuntimeIOException("should not occur", ex);
        }
    }

}
