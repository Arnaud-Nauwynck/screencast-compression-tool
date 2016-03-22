package fr.an.screencast.compressor.imgtool.ocr.settings;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.an.screencast.compressor.utils.Rect;

public class OCRGlyphDescr implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;

    private transient OCRSettings ownerSettings;
    
    private final String glyphDisplayName;
    private final String glyphText;
    
    private List<OCRGlyphConnexeComponent> connexComponents = new ArrayList<OCRGlyphConnexeComponent>(1);

    private Rect cachedEnclosingRectOffset;
    
    // ------------------------------------------------------------------------
    
    public OCRGlyphDescr(OCRSettings ownerSettings, String glyphDisplayName, String glyphText) {
        this.ownerSettings = ownerSettings;
        this.glyphDisplayName = glyphDisplayName;
        this.glyphText = glyphText;
    }

    // ------------------------------------------------------------------------
    
    public OCRSettings getOwnerSettings() {
        return ownerSettings;
    }

    public void _setOwnerSettings(OCRSettings p) {
        this.ownerSettings = p;
    }

    public String getGlyphDisplayName() {
        return glyphDisplayName;
    }

    public String getGlyphText() {
        return glyphText;
    }
    
    public List<OCRGlyphConnexeComponent> getConnexComponents() {
        return connexComponents;
    }
    
    public void addConnexComponent(OCRGlyphConnexeComponent connexComp) {
        if (connexComp.getOwnerGlyph() != this) {
            throw new IllegalArgumentException();
        }
        connexComponents.add(connexComp);
        cachedEnclosingRectOffset = null;
    }
    
    public Rect getEnclosingRectOffset() {
        if (cachedEnclosingRectOffset == null) {
            Rect res = new Rect(); // empty
            for(OCRGlyphConnexeComponent comp : connexComponents) {
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
        return "Glyph[" + glyphDisplayName + "]";
    }
    
}
