package fr.an.screencast.compressor.imgtool.color;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.screencast.compressor.dtos.color.ColorChannelStats;
import fr.an.screencast.compressor.dtos.color.ColorLRUChangeStats;
import fr.an.screencast.compressor.dtos.color.ColorMapAnalysisResult;
import fr.an.screencast.compressor.imgtool.utils.HSVColor;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.screencast.compressor.utils.BasicStats;
import fr.an.screencast.compressor.utils.ColorBarLookupTable;
import fr.an.screencast.compressor.utils.Dim;

public class ColorMapAnalysis implements Serializable {
    
    /** */
    private static final long serialVersionUID = 1L;


    private static final Logger LOG = LoggerFactory.getLogger(ColorMapAnalysis.class);
    
       
    private static final boolean USE_GLOBAL_COLOR_STATS = false;

    private final Dim dim;
    
    private ColorMapAnalysisResult result;
    
    private int frameIndex;
    private transient HSVColor hsvColor = new HSVColor();
    
    // ------------------------------------------------------------------------
    
    public ColorMapAnalysis(Dim dim, int colorLRUSize) {
        this.dim = dim;
        
        this.result = new ColorMapAnalysisResult(dim, USE_GLOBAL_COLOR_STATS, colorLRUSize);
    }

    // ------------------------------------------------------------------------

    public void processImage(BufferedImage imageRGB) {
        final int width = dim.width, height = dim.height;
        final int[] data = ImageRasterUtils.toInts(imageRGB);
        
        final ColorChannelStats globalColorChannelStats = result.getGlobalColorChannelStats();
        final ColorChannelStats[] locationToColorStats = result.getLocationToColorStats();
        final ColorLRUChangeStats[] locationToColorLRUChangeStats = result.getLocationToColorLRUChangeStats();
        
        final ColorModel cm = imageRGB.getColorModel();
        for(int y=0, idx=0; y < height; y++) {
            for (int x = 0; x < width; x++,idx++) {
                int rgb = data[idx];
                int red = cm.getRed(rgb);
                int green = cm.getGreen(rgb);
                int blue = cm.getBlue(rgb);
                int alpha = cm.getAlpha(rgb);
                
                hsvColor.setFromRGB(red, green, blue);
                
                if (USE_GLOBAL_COLOR_STATS) {
                    globalColorChannelStats.add(red, green, blue, alpha, hsvColor);
                }
                
                locationToColorStats[idx].add(red, green, blue, alpha, hsvColor);

                locationToColorLRUChangeStats[idx].addRGB(rgb, frameIndex);
                
//                // color bits reduction...
//                int highRed = red >>> 4;
//                int highGreen = green >>> 4;
//                int highBlue = blue >>> 4;
//                int highRGB = RGBUtils.rgb2Int(highRed, highGreen,  highBlue, 0);
//                
//                LocationStats locationStats = colorToLocationStats.get(highRGB);
//                if (locationStats == null) {
//                    locationStats = new LocationStats();
//                    colorToLocationStats.put(rgb, locationStats);
//                }
//                locationStats.add(frameIndex, x, y);
                
            }
        }
        
        frameIndex++;
    }

    public void dump() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(1000); 
        PrintStream out = new PrintStream(buffer);
        result.dump(out);
        out.close();
        
        LOG.info(buffer.toString());
    }
    

    public void debugDrawMinMax(ColorBarLookupTable colorBarLookupTable,
            BufferedImage minRGBImg, BufferedImage maxRGBImg, 
            BufferedImage minRImg, BufferedImage maxRImg,
            BufferedImage minGImg, BufferedImage maxGImg,
            BufferedImage minBImg, BufferedImage maxBImg,
            BufferedImage rangeImg, BufferedImage rangeRImg, BufferedImage rangeGImg, BufferedImage rangeBImg
            ) {
        final int width = dim.width, height = dim.height;
        int[] minRGBInts = ImageRasterUtils.toInts(minRGBImg);
        int[] maxRGBInts = ImageRasterUtils.toInts(maxRGBImg);
        
        int[] minRInts = ImageRasterUtils.toInts(minRImg);
        int[] maxRInts = ImageRasterUtils.toInts(maxRImg);
        int[] minGInts = ImageRasterUtils.toInts(minGImg);
        int[] maxGInts = ImageRasterUtils.toInts(maxGImg);
        int[] minBInts = ImageRasterUtils.toInts(minBImg);
        int[] maxBInts = ImageRasterUtils.toInts(maxBImg);
        
        int[] rangeInts = ImageRasterUtils.toInts(rangeImg);
        int[] rangeRInts = ImageRasterUtils.toInts(rangeRImg);
        int[] rangeGInts = ImageRasterUtils.toInts(rangeGImg);
        int[] rangeBInts = ImageRasterUtils.toInts(rangeBImg);
        
        for(int y=0, idx=0; y < height; y++) {
            for (int x = 0; x < width; x++,idx++) {
                minRGBInts[idx] = RGBUtils.CMAX_255; 
                maxRGBInts[idx] = 0;
                
                minRInts[idx] = RGBUtils.CMAX_255;
                maxRInts[idx] = 0;
                minGInts[idx] = RGBUtils.CMAX_255;
                maxGInts[idx] = 0;
                minBInts[idx] = RGBUtils.CMAX_255;
                maxBInts[idx] = 0;
            }
        }

        final ColorChannelStats[] locationToColorStats = result.getLocationToColorStats();
        for(int y=0, idx=0; y < height; y++) {
            for (int x = 0; x < width; x++,idx++) {
                ColorChannelStats ptColorStats = locationToColorStats[idx];
                BasicStats r = ptColorStats.getRedStats();
                BasicStats g = ptColorStats.getGreenStats();
                BasicStats b = ptColorStats.getBlueStats();
                
                // assemble minRGB = min(r),min(g),min(b)
                // assemble maxRGB = min(r),min(g),min(b)
                int rMin = r.getMin(), gMin = g.getMin(), bMin = b.getMin();
                int rMax = r.getMax(), gMax = g.getMax(), bMax = b.getMax();
                minRGBInts[idx] = RGBUtils.rgb2Int(rMin, gMin, bMin, 0);                 
                maxRGBInts[idx] = RGBUtils.rgb2Int(rMax, gMax, bMax, 0);
                
                minRInts[idx] = RGBUtils.rgb2Int(rMin, 0, 0, 0);
                maxRInts[idx] = RGBUtils.rgb2Int(rMax, 0, 0, 0);
                minGInts[idx] = RGBUtils.rgb2Int(0, gMin, 0, 0);
                maxGInts[idx] = RGBUtils.rgb2Int(0, gMax, 0, 0);
                minBInts[idx] = RGBUtils.rgb2Int(0, 0, bMin, 0);
                maxBInts[idx] = RGBUtils.rgb2Int(0, 0, bMax, 0);

                rangeInts[idx] = colorBarLookupTable.interpolateRGB(((rMax - rMin) + (gMax - gMin) + (bMax - bMin)) / 3, 0, RGBUtils.CMAX_255);
                rangeRInts[idx] = colorBarLookupTable.interpolateRGB(rMax - rMin, 0, RGBUtils.CMAX_255);
                rangeGInts[idx] = colorBarLookupTable.interpolateRGB(gMax - gMin, 0, RGBUtils.CMAX_255);
                rangeBInts[idx] = colorBarLookupTable.interpolateRGB(bMax - bMin, 0, RGBUtils.CMAX_255);
            }
        }
    }

    public void debugDrawLRUColorCounts(ColorBarLookupTable colorBarLookupTable, 
            BufferedImage countChangeImg, 
            BufferedImage countChangeLowImg,
            BufferedImage countChangeMidImg,
            BufferedImage countChangeHighImg 
            ) {
        final int width = dim.width, height = dim.height;
        final ColorLRUChangeStats[] locationToColorLRUChangeStats = result.getLocationToColorLRUChangeStats();

        int[] countChangeInts = ImageRasterUtils.toInts(countChangeImg);
        int[] countChangeLowInts = ImageRasterUtils.toInts(countChangeLowImg);
        int[] countChangeMidInts = ImageRasterUtils.toInts(countChangeMidImg);
        int[] countChangeHighInts = ImageRasterUtils.toInts(countChangeHighImg);

        int maxChange = 0;
        for(int y=0, idx=0; y < height; y++) {
            for (int x = 0; x < width; x++,idx++) {
                ColorLRUChangeStats lruChange = locationToColorLRUChangeStats[idx];
                maxChange = Math.max(maxChange, lruChange.getCountChange()); 
            }
        }
        LOG.info("max LRU Change:" + maxChange);
        int tail = 10;
        int thresholdLow = maxChange*tail/100;
        int thresholdHigh = maxChange*2*tail/100; // maxChange - maxChange*tail/100;
        LOG.info("thresholds for LRU Change: low " + tail + "%:" + thresholdLow 
            + " - high " + (100-tail) + "%: " + thresholdHigh);
        
        for(int y=0, idx=0; y < height; y++) {
            for (int x = 0; x < width; x++,idx++) {
                ColorLRUChangeStats lruChange = locationToColorLRUChangeStats[idx];
                int change = lruChange.getCountChange();

                countChangeInts[idx] = colorBarLookupTable.interpolateRGB(change, 0, maxChange);
                
                int changeLowRGB = 0;
                if (change < thresholdLow) {
                    changeLowRGB = colorBarLookupTable.interpolateRGB(change, 0, thresholdLow);
                }
                countChangeLowInts[idx] = changeLowRGB;

                int changeMidRGB = 0;
                if (thresholdLow <= change && change <= thresholdHigh) {
                    changeMidRGB = colorBarLookupTable.interpolateRGB(change, thresholdLow, thresholdHigh);
                }
                countChangeMidInts[idx] = changeMidRGB;

                int changeHighRGB = 0;
                if (change > thresholdHigh) {
                    changeHighRGB = colorBarLookupTable.interpolateRGB(change, thresholdHigh, maxChange);
                }
                countChangeHighInts[idx] = changeHighRGB;
            }
        }
        
    }
}
