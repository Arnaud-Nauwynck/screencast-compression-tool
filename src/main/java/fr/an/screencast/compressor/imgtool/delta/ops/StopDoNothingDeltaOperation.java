package fr.an.screencast.compressor.imgtool.delta.ops;

import java.awt.image.BufferedImage;

import fr.an.screencast.compressor.imgtool.delta.DeltaContext;
import fr.an.screencast.compressor.imgtool.delta.DeltaOperation;

/**
 * dummy marker class for encoding "stop" marker on list of DeltaOperation
 *
 */
public class StopDoNothingDeltaOperation extends DeltaOperation {

    /** */
    private static final long serialVersionUID = 1L;

    public StopDoNothingDeltaOperation() {
    }
    
    @Override
    public void apply(DeltaContext context, BufferedImage dest) {
        // do nothing
    }
    
}
