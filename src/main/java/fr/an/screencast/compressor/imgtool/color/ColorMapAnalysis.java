package fr.an.screencast.compressor.imgtool.color;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.screencast.compressor.imgtool.utils.HSVColor;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.screencast.compressor.utils.BasicStats;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.LocationStats;

public class ColorMapAnalysis {
    
    private static final Logger LOG = LoggerFactory.getLogger(ColorMapAnalysis.class);
    
    private final Dim dim;
    
    static final int C_LOW = 13;
    static final int C_HIGH = 242;
    
    private static final boolean USE_GLOBAL_COLOR_STATS = false;
    private ColorChannelStats globalColorChannelStats = new ColorChannelStats();
    
//    // color are restricted to a region?
//    private Map<Integer,LocationStats> colorToLocationStats = new HashMap<>(10000001);

    private ColorChannelStats[] locationToColorStats; 

    // color map restriction per pixel/area? (using LRU of colors)
    ColorLRUChangeStats[] locationToColorLRUChangeStats;
    
    private int frameIndex;
    private HSVColor hsvColor = new HSVColor();
    
    // ------------------------------------------------------------------------
    
    public ColorMapAnalysis(Dim dim, int colorLRUSize) {
        this.dim = dim;
        
        int len = dim.width * dim.height;//TODO reduce x,y precision
        locationToColorStats = new ColorChannelStats[len];
        for (int i = 0; i < len; i++) {
            locationToColorStats[i] = new ColorChannelStats();
        }
        locationToColorLRUChangeStats = new ColorLRUChangeStats[len];
        for (int i = 0; i < len; i++) {
            locationToColorLRUChangeStats[i] = new ColorLRUChangeStats(colorLRUSize);
        }
        
    }

    // ------------------------------------------------------------------------

    public void processImage(BufferedImage imageRGB) {
        final int width = dim.width, height = dim.height;
        final int[] data = ImageRasterUtils.toInts(imageRGB);
        
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
        dump(out);
        out.close();
        
        LOG.info(buffer.toString());
    }
    
    public void dump(PrintStream out) {
        final int width = dim.width, height = dim.height;
        out.println("colorChannelStats: ");
        if (USE_GLOBAL_COLOR_STATS) {
            out.print("RED: ");
            globalColorChannelStats.redStats.dump(out);
            out.println();
            out.print("GREEN: ");
            globalColorChannelStats.greenStats.dump(out);
            out.println();
            out.print("BLUE: ");
            globalColorChannelStats.blueStats.dump(out);
            out.println();
        }
        
        
        // locationToColorLRUChangeStats
        out.println("locationToColorMRUChangeStats:");
        int displayMaxCount = 500;
        int[] histoSmallChangeCounts = new int[displayMaxCount+1];
        int totalCountChanges = 0;
        for(int y=0, idx=0; y < height; y++) {
            for (int x = 0; x < width; x++,idx++) {
                int countChange = locationToColorLRUChangeStats[idx].getCountChange();
                totalCountChanges += countChange; 
                if (countChange < displayMaxCount) {
                    histoSmallChangeCounts[countChange]++;
                } else {
                    histoSmallChangeCounts[displayMaxCount]++;
                }
            }
        }
        out.println("display histogram for locationToColorLRUChangeStats, totalCountChanges:" + totalCountChanges);
        for(int i = 0; i < displayMaxCount; i++) {
            out.print(String.format("%4d", histoSmallChangeCounts[i]));
            out.print(" ");
        }
        out.print(" ..remain: " + histoSmallChangeCounts[displayMaxCount]);
        
        
        // dump special pt in locationToColorStats
        int countSpecial = 0;
        for(int y=0, idx=0; y < height; y+=20) {
            int countRowSpecial = 0;
            for (int x = 0; x < width; x+=20,idx+=20) {
                ColorChannelStats ptColorStats = locationToColorStats[idx];
                if (ptColorStats.isSpecial()) {
                    out.print("[" + x + "," + y + "]:");
                    ptColorStats.dumpSpecial(out);
                    countRowSpecial++;
                    
                    if (countRowSpecial % 100 == 0) {
                        out.println();
                        out.print("       ");
                    } else {
                        out.print(" ");
                    }
                }
            }
            if (countRowSpecial != 0) {
                out.println();
                countSpecial += countRowSpecial;
            }
        }
        out.println();
        out.println("found " + countSpecial + " / " + (height/20*width/20) + " special pts (sub-sampling x20) with special min-max color channel restriction");
    }

    public void debugDrawMinMax(BufferedImage minRGBImg, BufferedImage maxRGBImg, 
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
        
        for(int y=0, idx=0; y < height; y++) {
            for (int x = 0; x < width; x++,idx++) {
                ColorChannelStats ptColorStats = locationToColorStats[idx];
                BasicStats r = ptColorStats.redStats;
                BasicStats g = ptColorStats.greenStats;
                BasicStats b = ptColorStats.blueStats;
                
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

                rangeInts[idx] = RGBUtils.greyRgb2Int(((rMax - rMin) + (gMax - gMin) + (bMax - bMin)) / 3);
                rangeRInts[idx] = RGBUtils.rgb2Int(rMax - rMin, 0, 0, 0);
                rangeGInts[idx] = RGBUtils.rgb2Int(0, gMax - gMin, 0, 0);
                rangeBInts[idx] = RGBUtils.rgb2Int(0, 0, bMax - bMin, 0);
            }
        }
    }

    public void debugDrawLRUColorCounts(BufferedImage countChangeImg, 
            BufferedImage countChangeLowImg,
            BufferedImage countChangeMidImg,
            BufferedImage countChangeHighImg 
            ) {
        final int width = dim.width, height = dim.height;
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
        int thresholdLow = maxChange/10; // <10%
        int thresholdHigh = maxChange - maxChange/10; // >90%
        LOG.info("thresholds for LRU Change: low 10%:" + thresholdLow + " - high 90%: " + thresholdHigh);
        
        for(int y=0, idx=0; y < height; y++) {
            for (int x = 0; x < width; x++,idx++) {
                ColorLRUChangeStats lruChange = locationToColorLRUChangeStats[idx];
                int change = lruChange.getCountChange();
                int change255 = (int) ((long)maxChange * 255 / maxChange);
                countChangeInts[idx] = RGBUtils.greyRgb2Int(change255);
                
                int changeLowRGB = 0;
                if (change < thresholdLow) {
                    int grey = RGBUtils.toByte256(55 + 200 * change / thresholdLow); 
                    changeLowRGB = RGBUtils.greyRgb2Int(grey);
                }
                countChangeLowInts[idx] = changeLowRGB;

                int changeMidRGB = 0;
                if (thresholdLow <= change && change <= thresholdHigh) {
                    // toByte256();
                    int grey = 255 * (change - thresholdLow) / (thresholdHigh - thresholdLow); 
                    changeMidRGB = RGBUtils.greyRgb2Int(grey);
                }
                countChangeMidInts[idx] = changeMidRGB;

                int changeHighRGB = 0;
                if (change > thresholdHigh) {
                    int grey =  RGBUtils.toByte256(55 + 200 * (change - thresholdHigh) / thresholdHigh);
                    changeHighRGB = RGBUtils.greyRgb2Int(grey);
                }
                countChangeHighInts[idx] = changeHighRGB;
            }
        }
        
    }
}
