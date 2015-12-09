package fr.an.screencast.compressor.imgtool.color;

import java.io.PrintStream;
import java.io.Serializable;

import fr.an.screencast.compressor.imgtool.delta.IntValueLRUChangeHistory;
import fr.an.screencast.compressor.utils.Dim;

public class ColorMapAnalysisResult implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;

    private final Dim dim;
    private ColorChannelStats globalColorChannelStats;

    // // color are restricted to a region?
    // private Map<Integer,LocationStats> colorToLocationStats = new
    // HashMap<>(10000001);

    private ColorChannelStats[] locationToColorStats;

    // color map restriction per pixel/area? (using LRU of colors)
    private IntValueLRUChangeHistory[] locationToColorLRUChangeStats;

    // ------------------------------------------------------------------------

    public ColorMapAnalysisResult(Dim dim, boolean usGlobalColorStats, int colorLRUSize) {
        this.dim = dim;
        int len = dim.width * dim.height;//TODO reduce x,y precision
        if (usGlobalColorStats) {
            globalColorChannelStats = new ColorChannelStats();
        }
        locationToColorStats = new ColorChannelStats[len];
        for (int i = 0; i < len; i++) {
            locationToColorStats[i] = new ColorChannelStats();
        }
        locationToColorLRUChangeStats = new IntValueLRUChangeHistory[len];
        for (int i = 0; i < len; i++) {
            locationToColorLRUChangeStats[i] = new IntValueLRUChangeHistory(colorLRUSize);
        }
    }

    // ------------------------------------------------------------------------

    public ColorChannelStats getGlobalColorChannelStats() {
        return globalColorChannelStats;
    }

    public ColorChannelStats[] getLocationToColorStats() {
        return locationToColorStats;
    }

    public IntValueLRUChangeHistory[] getLocationToColorLRUChangeStats() {
        return locationToColorLRUChangeStats;
    }

    public void dump(PrintStream out) {
        final int width = dim.width, height = dim.height;
        out.println("colorChannelStats: ");
        if (globalColorChannelStats != null) {
            out.print("RED: ");
            globalColorChannelStats.getRedStats().dump(out);
            out.println();
            out.print("GREEN: ");
            globalColorChannelStats.getGreenStats().dump(out);
            out.println();
            out.print("BLUE: ");
            globalColorChannelStats.getBlueStats().dump(out);
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

    // ------------------------------------------------------------------------
    
    @Override
    public String toString() {
        return "ColorMapAnalysisResult ["
                // globalColorChannelStats=" + globalColorChannelStats 
                // + ", locationToColorStats=" + Arrays.toString(locationToColorStats) 
                // + ", locationToColorLRUChangeStats=" + Arrays.toString(locationToColorLRUChangeStats) 
                + "]";
    }

}
