package fr.an.screencast.compressor.dtos.delta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DeltaImageAnalysisResult implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;
    
    private List<FrameDelta> frameDeltas = new ArrayList<FrameDelta>(100);

    // ------------------------------------------------------------------------

    public DeltaImageAnalysisResult() {
    }

    // ------------------------------------------------------------------------
    
    public List<FrameDelta> getFrameDeltas() {
        return frameDeltas;
    }

    public void addFrameDelta(FrameDelta p) {
        frameDeltas.add(p);
    }
    
    // ------------------------------------------------------------------------
    
}
