package fr.an.screencast.compressor.imgtool.delta;

import fr.an.screencast.compressor.imgtool.color.ColorToLocationStatsMap;

public class FrameRectDeltaDetailed {

    private FrameRectDelta resultFrameRectDelta;
    
    private ColorToLocationStatsMap colorStats;
    
    // ------------------------------------------------------------------------

    public FrameRectDeltaDetailed() {
    }

    // ------------------------------------------------------------------------
    
    public FrameRectDelta getResultFrameRectDelta() {
        return resultFrameRectDelta;
    }

    public void setResultFrameRectDelta(FrameRectDelta resultFrameRectDelta) {
        this.resultFrameRectDelta = resultFrameRectDelta;
    }

    public ColorToLocationStatsMap getColorStats() {
        return colorStats;
    }

    public void setColorStats(ColorToLocationStatsMap colorStats) {
        this.colorStats = colorStats;
    }
    
}
