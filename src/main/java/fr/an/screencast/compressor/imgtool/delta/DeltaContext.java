package fr.an.screencast.compressor.imgtool.delta;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import fr.an.screencast.compressor.imgtool.glyph.GlyphMRUTable;

public class DeltaContext {

    private int frameIndex;
    private BufferedImage prevImage;
    
    private IntImageLRUChangeHistory imageLRUChangeHistory;

    private GlyphMRUTable glyphMRUTable;
    
    private Map<String,Object> props = new HashMap<String,Object>();
    
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
    
    public void putProp(String key, Object value) {
        props.put(key, value);
    }

    public Object getProp(String key) {
        return props.get(key);
    }

}
