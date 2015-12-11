package fr.an.screencast.compressor.imgtool.delta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.an.screencast.compressor.utils.Rect;

public class FrameDelta implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;
    
    private final int frameIndex;
    private List<FrameRectDelta> deltas = new ArrayList<FrameRectDelta>();

    // ------------------------------------------------------------------------

    public FrameDelta(int frameIndex) {
        this.frameIndex = frameIndex;
    }
    
    // ------------------------------------------------------------------------
    
    public int getFrameIndex() {
        return frameIndex;
    }
    
    public void addFrameRectDelta(FrameRectDelta p) {
        deltas.add(p);
    }

    public void addFrameRectDeltas(Collection<Rect> rects) {
        for(Rect rect : rects) {
            addFrameRectDelta(new FrameRectDelta(this, rect));
        }
    }
    
    
    public List<FrameRectDelta> getDeltas() {
        return deltas;
    }

    @Override
    public String toString() {
        return "FrameDelta [frameIndex=" + frameIndex + ", deltas.length:" + deltas.size() + "]";
    }
    
    
}