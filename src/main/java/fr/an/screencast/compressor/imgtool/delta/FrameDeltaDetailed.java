package fr.an.screencast.compressor.imgtool.delta;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class FrameDeltaDetailed {
    
    private int frameIndex;
    private List<FrameRectDeltaDetailed> frameRectDeltaDetaileds = new ArrayList<FrameRectDeltaDetailed>();
    
    private FrameDelta resultFrameDelta;
    
    private BufferedImage colorReduceImg;

    // ------------------------------------------------------------------------

    public FrameDeltaDetailed() {
    }

    // ------------------------------------------------------------------------

    public int getFrameIndex() {
        return frameIndex;
    }

    public void setFrameIndex(int frameIndex) {
        this.frameIndex = frameIndex;
    }

    public FrameDelta getResultFrameDelta() {
        return resultFrameDelta;
    }

    public void setResultFrameDelta(FrameDelta resultFrameDelta) {
        this.resultFrameDelta = resultFrameDelta;
    }
    

    public List<FrameRectDeltaDetailed> getFrameRectDeltaDetaileds() {
        return frameRectDeltaDetaileds;
    }

    public void setFrameRectDeltaDetaileds(List<FrameRectDeltaDetailed> frameRectDeltaDetaileds) {
        this.frameRectDeltaDetaileds = frameRectDeltaDetaileds;
    }

    
    
    public BufferedImage getColorReduceImg() {
        return colorReduceImg;
    }

    public void setColorReduceImg(BufferedImage colorReduceImg) {
        this.colorReduceImg = colorReduceImg;
    }

    public void addFrameRectDelta(FrameRectDeltaDetailed rectDeltaDetailed) {
        frameRectDeltaDetaileds.add(rectDeltaDetailed);
    }

    
}
