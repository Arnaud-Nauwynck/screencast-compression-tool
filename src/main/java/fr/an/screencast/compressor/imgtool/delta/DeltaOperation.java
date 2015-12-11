package fr.an.screencast.compressor.imgtool.delta;

import java.awt.image.BufferedImage;
import java.io.Serializable;

public abstract class DeltaOperation implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;

    public abstract void apply(DeltaContext context, BufferedImage dest);
    
}
