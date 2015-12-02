package fr.an.screencast.compressor.utils;

public final class LocationStats {
    
    private int count;
    
    private final BasicStats xStats = new BasicStats(); 
    private final BasicStats yStats = new BasicStats();
    
    private final BasicStats frameIndexStats = new BasicStats();
    
    // ------------------------------------------------------------------------

    public LocationStats() {
    }

    // ------------------------------------------------------------------------
    
    public void add(int frameIndex, int x, int y) {
        count++;
        xStats.add(x);
        yStats.add(y);
        frameIndexStats.add(frameIndex);
    }

    public int getCount() {
        return count;
    }

    public BasicStats getxStats() {
        return xStats;
    }

    public BasicStats getyStats() {
        return yStats;
    }

    public BasicStats getFrameIndexStats() {
        return frameIndexStats;
    }
    
    
}