package fr.an.screencast.compressor.imgtool.color;

import java.io.PrintStream;
import java.io.Serializable;

import fr.an.screencast.compressor.imgtool.utils.HSVColor;
import fr.an.screencast.compressor.utils.BasicStats;

public class ColorChannelStats implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;
    
    private final BasicStats redStats = new BasicStats();
    private final BasicStats greenStats = new BasicStats();
    private final BasicStats blueStats = new BasicStats();
    private final BasicStats alphaStats = new BasicStats();

    private int whiteCount;
    private int blackCount;
    // private final BasicStats redNoWBStats = new BasicStats();
    // private final BasicStats greenNoWBStats = new BasicStats();
    // private final BasicStats blueNoWBStats = new BasicStats();

    // ------------------------------------------------------------------------

    public ColorChannelStats() {
    }

    // ------------------------------------------------------------------------
    
    public void add(int r, int g, int b, int a, HSVColor rgb2hsvHelper) {
        int white = (r + g + b) / 3;

        if (white > ColorMapAnalysis.C_HIGH) {
            whiteCount++;
        } else if (white < ColorMapAnalysis.C_LOW) {
            blackCount++;
        } else {
            // redNoWBStats.add(r);
            // greenNoWBStats.add(g);
            // blueNoWBStats.add(b);

        }

        redStats.add(r);
        greenStats.add(g);
        blueStats.add(b);

        // stats on HSV

        // alphaStats.add(a); // TODO
    }

    public void dumpSpecial(PrintStream out) {
        // if (whiteCount > 0) {
        // out.print(" w# " + whiteCount);
        // }
        // if (blackCount > 0) {
        // out.print(" b# " + blackCount);
        // }
        redStats.optDumpSpecial(out, " R", ColorMapAnalysis.C_LOW + 1, ColorMapAnalysis.C_HIGH - 1);
        greenStats.optDumpSpecial(out, " G", ColorMapAnalysis.C_LOW + 1, ColorMapAnalysis.C_HIGH - 1);
        blueStats.optDumpSpecial(out, " B", ColorMapAnalysis.C_LOW + 1, ColorMapAnalysis.C_HIGH - 1);
    }

    public boolean isSpecial() {
        // if (whiteCount > 0) {
        // return true;
        // }
        // if (blackCount > 0) {
        // return true;
        // }
        if (redStats.isSpecial(ColorMapAnalysis.C_LOW, ColorMapAnalysis.C_HIGH)
            || greenStats.isSpecial(ColorMapAnalysis.C_LOW, ColorMapAnalysis.C_HIGH)
            || blueStats.isSpecial(ColorMapAnalysis.C_LOW, ColorMapAnalysis.C_HIGH)) {
            return true;
        }
        return false;
    }

    public BasicStats getRedStats() {
        return redStats;
    }

    public BasicStats getGreenStats() {
        return greenStats;
    }

    public BasicStats getBlueStats() {
        return blueStats;
    }

    public BasicStats getAlphaStats() {
        return alphaStats;
    }

    public int getWhiteCount() {
        return whiteCount;
    }

    public int getBlackCount() {
        return blackCount;
    }
    
    
}