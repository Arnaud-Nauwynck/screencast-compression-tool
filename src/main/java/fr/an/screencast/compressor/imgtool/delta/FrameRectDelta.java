package fr.an.screencast.compressor.imgtool.delta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.an.screencast.compressor.utils.Rect;

public class FrameRectDelta implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;
    
    private transient FrameDelta parentFrame;
    private Rect rect;

    private List<DeltaOperation> deltaOperations = new ArrayList<DeltaOperation>();
    
    // ------------------------------------------------------------------------

    public FrameRectDelta(FrameDelta parentFrame, Rect rect) {
        this.parentFrame = parentFrame;
        this.rect = new Rect(rect);
    }

    // ------------------------------------------------------------------------

    public Rect getRect() {
        return rect;
    }

    public List<DeltaOperation> getDeltaOperations() {
        return deltaOperations;
    }

    public void addDeltaOperation(DeltaOperation p) {
        deltaOperations.add(p);
    }

}