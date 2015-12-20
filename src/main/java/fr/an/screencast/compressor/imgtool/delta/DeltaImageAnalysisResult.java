package fr.an.screencast.compressor.imgtool.delta;

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

    public FrameDelta getFrameDelta(int frameIndex) {
        FrameDelta res = null;
        for(int i = frameDeltas.size()-1; i >= 0; i--) {        
            FrameDelta tmpres = frameDeltas.get(i);
            if (tmpres.getFrameIndex() == frameIndex) {
                res = tmpres;
                break;
            }
        }
        return res;
    }
    
    // ------------------------------------------------------------------------
    
}
