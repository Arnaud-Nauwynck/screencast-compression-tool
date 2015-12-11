package fr.an.screencast.compressor.imgtool.delta;

import java.awt.image.BufferedImage;

public class DeltaContext {

    private int frameIndex;
    private BufferedImage prevImage;
    
    private IntImageLRUChangeHistory imageLRUChangeHistory;

    // ------------------------------------------------------------------------
    
    public DeltaContext(int frameIndex, BufferedImage prevImage, IntImageLRUChangeHistory imageLRUChangeHistory) {
        this.frameIndex = frameIndex;
        this.prevImage = prevImage;
        this.imageLRUChangeHistory = imageLRUChangeHistory;
    }

    // ------------------------------------------------------------------------

    public int getFrameIndex() {
        return frameIndex;
    }
    
    public BufferedImage getPrevImage() {
        return prevImage;
    }

    public IntImageLRUChangeHistory getImageLRUChangeHistory() {
        return imageLRUChangeHistory;
    }

    
}
