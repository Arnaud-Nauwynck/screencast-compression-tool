package fr.an.screencast.compressor.utils;

import java.io.PrintStream;

public class ProgessPrinter {

    private PrintStream out;
    private int frameRate;
    private int displayProgressFrequency;
    private int displayIndexNumberFrequency;

    
    private int iterIndex;

    private int iterProgressModulo; // = iterIndex % displayProgressFrequency
    private int iterIndexNumberModulo; // =  iterIndex % displayIndexNumberFrequency

    // ------------------------------------------------------------------------

    public ProgessPrinter(PrintStream out, int frameRate, int displayProgressFrequency, int displayIndexNumberFrequency) {
        this.out = out;
        this.frameRate = frameRate;
        this.displayProgressFrequency = displayProgressFrequency;
        this.displayIndexNumberFrequency = displayIndexNumberFrequency;
    }

    // ------------------------------------------------------------------------

    public void reset() {
        iterIndex = 0;
        iterProgressModulo = 0;
        iterIndexNumberModulo = 0;
    }
    
    public void next() {
        iterIndex++;
        if (displayIndexNumberFrequency != 1) {
            iterProgressModulo++;
            if (iterProgressModulo == displayProgressFrequency) {
                iterProgressModulo = 0;
                out.print(".");
            }
        }
        iterIndexNumberModulo++;
        if (iterIndexNumberModulo == displayIndexNumberFrequency) {
            iterIndexNumberModulo = 0;
            out.print("\n[" + iterIndex + "] ");
        }
    }

    public int getDisplayProgressFrequency() {
        return displayProgressFrequency;
    }

    public int getDisplayIndexNumberFrequency() {
        return displayIndexNumberFrequency;
    }

    public String toStringFrequencyInfo() {
        return "display progress every " + displayProgressFrequency + " frames = " + (displayProgressFrequency/frameRate) + " s";
    }
    
    @Override
    public String toString() {
        return "ProgessPrinter [" + iterIndex + "]";
    }

    
}
