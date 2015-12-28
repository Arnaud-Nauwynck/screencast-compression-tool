package fr.an.screencast.compressor.imgtool.delta;

import java.awt.image.BufferedImage;

import fr.an.screencast.compressor.imgtool.glyph.GlyphMRUTable;

public class DeltaContext {

    private int frameIndex;
    private BufferedImage prevImage;
    
    private IntImageLRUChangeHistory imageLRUChangeHistory;

    private GlyphMRUTable glyphMRUTable;
    
    // ------------------------------------------------------------------------
    
    public DeltaContext(int frameIndex, BufferedImage prevImage, 
            IntImageLRUChangeHistory imageLRUChangeHistory,
            GlyphMRUTable glyphMRUTable) {
        this.frameIndex = frameIndex;
        this.prevImage = prevImage;
        this.imageLRUChangeHistory = imageLRUChangeHistory;
        this.glyphMRUTable = glyphMRUTable;
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

    public GlyphMRUTable getGlyphMRUTable() {
        return glyphMRUTable;
    }
    
}
