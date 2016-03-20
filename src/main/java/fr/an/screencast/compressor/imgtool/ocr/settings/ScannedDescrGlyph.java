package fr.an.screencast.compressor.imgtool.ocr.settings;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.an.screencast.compressor.utils.Rect;

public class ScannedDescrGlyph implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;

    private final String glyphDisplayName;
    private final String glyphText;
    
    private List<ScannedDescrConnexeComponent> scannedConnexeComponents = new ArrayList<ScannedDescrConnexeComponent>();

    private Rect cachedEnclosingRectOffset;
    
    // ------------------------------------------------------------------------
    
    public ScannedDescrGlyph(String glyphDisplayName, String glyphText) {
        this.glyphDisplayName = glyphDisplayName;
        this.glyphText = glyphText;
    }

    // ------------------------------------------------------------------------
    
    public String getGlyphDisplayName() {
        return glyphDisplayName;
    }

    public String getGlyphText() {
        return glyphText;
    }
    
    public List<ScannedDescrConnexeComponent> getScannedConnexeComponents() {
        return scannedConnexeComponents;
    }
    
    public Rect getEnclosingRectOffset() {
        if (cachedEnclosingRectOffset == null) {
            Rect res = new Rect(); // empty
            for(ScannedDescrConnexeComponent comp : scannedConnexeComponents) {
                Rect r = comp.getRectOffset();
                res.setDilateToContain(r);
            }
            cachedEnclosingRectOffset = res;
        }
        return cachedEnclosingRectOffset;
    }

    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return "ScannedDescrGlyph[" + glyphDisplayName + "]";
    }
    
}
