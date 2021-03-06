package fr.an.screencast.compressor.imgtool.ocr.settings;

import java.io.File;
import java.io.Serializable;

import fr.an.screencast.compressor.imgtool.utils.ImageData;
import fr.an.screencast.compressor.imgtool.utils.ImageIOUtils;
import fr.an.screencast.compressor.imgtool.utils.IntsCRC32;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.Rect;

public class OCRGlyphConnexeComponent implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;
    
    private transient OCRGlyphDescr ownerGlyph;
    private final Pt offset;
    
    private final String imageDataFilename;
    
    // private final Dim dim;
    private transient ImageData imageData; 
    
    private transient int crc32;
    
    // ------------------------------------------------------------------------
    
    public OCRGlyphConnexeComponent(OCRGlyphDescr ownerGlyph, Pt offset, String imageDataFilename) {
        this.ownerGlyph = ownerGlyph;
        this.offset = offset;
        this.imageDataFilename = imageDataFilename;
    }
    
    // ------------------------------------------------------------------------
    
    public OCRGlyphDescr getOwnerGlyph() {
        return ownerGlyph;
    }

    /*pp*/ void _setOwnerGlyph(OCRGlyphDescr ownerGlyph) {
        this.ownerGlyph = ownerGlyph;
    }
    

    public Pt getOffset() {
        return offset;
    }

    public Rect getRectOffset() {
        return Rect.newPtDim(offset, getDim());
    }
    
    public String getImageDataFilename() {
        return imageDataFilename;
    }

    public ImageData getImageData() {
        if (imageData == null) {
            File baseDir = new File(ownerGlyph.getOwnerSettings().getBaseDir());
            imageData = ImageIOUtils.readRGBAImageData(new File(baseDir, imageDataFilename));
        }
        return imageData;
    }

    public Dim getDim() {
        return getImageData().getDim();
    }
    
    public int getCrc32() {
        if (crc32 == 0) {
            crc32 = IntsCRC32.crc32(getImageData().getData());
        }
        return crc32;
    }

    // ------------------------------------------------------------------------
    
    @Override
    public String toString() {
        return "ScannedDescrConnexeComponent [offset=" + offset + ", imageDataFilename=" + imageDataFilename + "]";
    }

}
