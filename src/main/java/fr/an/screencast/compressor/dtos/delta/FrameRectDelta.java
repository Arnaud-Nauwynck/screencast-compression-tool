package fr.an.screencast.compressor.dtos.delta;

import java.io.Serializable;

import fr.an.screencast.compressor.utils.Rect;

public class FrameRectDelta implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;
    
    private transient FrameDelta parentFrame;
    private Rect rect;

    // ------------------------------------------------------------------------

    public FrameRectDelta(FrameDelta parentFrame, Rect rect) {
        this.parentFrame = parentFrame;
        this.rect = new Rect(rect);
    }

    // ------------------------------------------------------------------------

    public Rect getRect() {
        return rect;
    }
            
    
}