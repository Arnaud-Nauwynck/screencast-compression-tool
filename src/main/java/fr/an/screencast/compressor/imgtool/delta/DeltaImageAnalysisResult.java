package fr.an.screencast.compressor.imgtool.delta;

import java.io.Serializable;

public class DeltaImageAnalysisResult implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;
    
    // DO NOT store all frames!... OutOfMemoryError..
    // private List<FrameDelta> frameDeltas = new ArrayList<FrameDelta>(100);

    private FrameDelta lastFrameDelta;
    
    // ------------------------------------------------------------------------

    public DeltaImageAnalysisResult() {
    }

    // ------------------------------------------------------------------------
    
    public FrameDelta getLastFrameDelta() {
        return lastFrameDelta;
    }
    
    public void addFrameDelta(FrameDelta p) {
        // frameDeltas.add(p);
        lastFrameDelta = p;
    }

    public FrameDelta getFrameDelta(int frameIndex) {
//        FrameDelta res = null;
//        for(int i = frameDeltas.size()-1; i >= 0; i--) {        
//            FrameDelta tmpres = frameDeltas.get(i);
//            if (tmpres.getFrameIndex() == frameIndex) {
//                res = tmpres;
//                break;
//            }
//        }
        if (lastFrameDelta != null && lastFrameDelta.getFrameIndex() != frameIndex) {
            return null; // should not occur?
        }
        return lastFrameDelta;
    }
    
    // ------------------------------------------------------------------------
    
}
