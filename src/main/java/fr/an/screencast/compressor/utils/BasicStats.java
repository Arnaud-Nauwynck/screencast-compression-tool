package fr.an.screencast.compressor.utils;

import java.io.PrintStream;
import java.io.Serializable;

public final class BasicStats implements Serializable {
    
    /** */
    private static final long serialVersionUID = 1L;
    
    private int min = Integer.MAX_VALUE;
    private int max = Integer.MIN_VALUE;
    
    private long count;
    private long sum;
//    private long sumSquare;
    
    // ------------------------------------------------------------------------

    public BasicStats() {
    }

    // ------------------------------------------------------------------------

    public void add(int value) {
        min = Math.min(min, value);
        max = Math.max(max, value);
        max = Math.max(max, value);
        count++;
        sum += value;
//        sumSquare += value*value;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public long getCount() {
        return count;
    }

    public long getSum() {
        return sum;
    }

    public double getAverage() {
        return (count != 0)? (double)sum /(double)count : 0.0;
    }
    
//    public long getSumSquare() {
//        return sumSquare;
//    }

    public void dump(PrintStream out) {
        out.print("min:" + min + ", max:" + max + " count:" + count + " avg:" + getAverage());
    }

    public void optDumpSpecial(PrintStream out, String label, int colorMin, int colorMax) {
        if (isSpecial(colorMin, colorMax)) {
            out.print(label);
            dumpSpecial(out, colorMin, colorMax);
        }
    }

    public void dumpSpecial(PrintStream out, int colorMin, int colorMax) {
        if (min > colorMin) {
            out.print(min);
        }
        out.print("<");
        if (max < colorMax) {
            out.print(max);
        }
    }

    public boolean isSpecial(int colorMin, int colorMax) {
        return min > colorMin || max < colorMax;
    }
    
}