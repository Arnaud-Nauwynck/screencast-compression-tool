package fr.an.screencast.compressor.imgtool.color;

import java.awt.image.BufferedImage;

import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.imgtool.utils.MorphologicImgRasterUtils;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.screencast.compressor.utils.Rect;

public class ColorBitsReducer {

    private final int openSize;
    private final int rgbLeastSignificantBitsMask;
    
    
    public ColorBitsReducer(int openSize, int nbRemovedColorBits) {
        this.openSize = openSize;
        int colorMask = (0xff >>> nbRemovedColorBits) << nbRemovedColorBits;
        this.rgbLeastSignificantBitsMask = RGBUtils.rgb2Int(colorMask, colorMask, colorMask);
    }

    public void processImage(BufferedImage destRGB, BufferedImage tmpRGB, BufferedImage srcRGB, final Rect roi) {
        final int[] destData = ImageRasterUtils.toInts(destRGB);
        final int[] tmpData = ImageRasterUtils.toInts(tmpRGB);
        final int[] srcData = ImageRasterUtils.toInts(srcRGB);
        final int width = srcRGB.getWidth(), height = srcRGB.getHeight();

        // TODO assume roi is strictly within image dimension!!
        ImageRasterUtils.copyRect(destData, srcData, width, height, roi);

        // apply openSize x morphological opening (min then max) of RGB color
        // => will remove "noise" (anti-aliasing gradient colors on objects borders)
        for (int repeatOpen = 0; repeatOpen < openSize; repeatOpen++) {
            MorphologicImgRasterUtils.erodeMinRGB(tmpData, destData, width, height, roi);
            MorphologicImgRasterUtils.dilateMaxRGB(destData, tmpData, width, height, roi);
        }
        
        // remove least significant bits
        maskColorBits(width, destData, rgbLeastSignificantBitsMask, roi);
    }
    
    public int reduceRGBLeastSignificantBits(int value) {
        return value & rgbLeastSignificantBitsMask;
    }
    
    public static void maskColorBits(final int width, int[] img, int colorBitMask, final Rect roi) {
        int idx = roi.fromY*width+roi.fromX;
        final int incrIdxY = roi.fromY + roi.fromX - roi.toX;
        for (int y = roi.fromY; y < roi.toY; y++,idx+=incrIdxY) {
            for (int x = roi.fromX; x < roi.toX; x++,idx++) {
                img[idx] = img[idx] & colorBitMask; 
            }
        }
    }
    
    public static void maskColorBits(int[] img, int colorBitMask) {
        final int len = img.length;
        for (int idx = 0; idx != len; idx++) {
            img[idx] = img[idx] & colorBitMask; 
        }
    }

    public static void maskLeastSignificantBits(int[] img, int nbRemovedColorBits) {
        int colorMask = (0xff >>> nbRemovedColorBits) << nbRemovedColorBits;
        int rgbLeastSignificantBitsMask = RGBUtils.rgb2Int(colorMask, colorMask, colorMask);
        maskColorBits(img, rgbLeastSignificantBitsMask);
    }

}
