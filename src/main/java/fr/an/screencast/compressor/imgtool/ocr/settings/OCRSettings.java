package fr.an.screencast.compressor.imgtool.ocr.settings;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.an.screencast.compressor.imgtool.utils.ImageData;
import fr.an.screencast.compressor.imgtool.utils.IntsCRC32;
import fr.an.screencast.compressor.utils.MapUtils;

/**
 * POJO for storing OCR settings with glyphs descriptions (scanned connexe components)
 */
public class OCRSettings implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;
    
    private String baseDir = ".";
    
    private List<OCRGlyphDescr> glyphDescrs = new ArrayList<>();
    
    private transient Map<Integer/*Crc32*/,ArrayList<OCRGlyphConnexeComponent>> cacheCrc32ToGlyphConnexeComps;
    
    // ------------------------------------------------------------------------

    public OCRSettings() {
    }

    // ------------------------------------------------------------------------

    public String getBaseDir() {
        return baseDir;
    }
    
    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }
    
    
    public List<OCRGlyphDescr> getGlyphDescrs() {
        return glyphDescrs;
    }

    public void addGlyphDescr(OCRGlyphDescr glyph) {
        glyphDescrs.add(glyph);
        purgeCache();
    }
    
    public void purgeCache() {
        cacheCrc32ToGlyphConnexeComps = null;
    }
    
    public Map<Integer/*Crc32*/,ArrayList<OCRGlyphConnexeComponent>> getCrc32ToGlyphConnexeComps() {
        if (cacheCrc32ToGlyphConnexeComps == null) {
            Map<Integer/*Crc32*/,ArrayList<OCRGlyphConnexeComponent>> res = new HashMap<>();
            for(OCRGlyphDescr glyph : glyphDescrs) {
                for(OCRGlyphConnexeComponent comp : glyph.getConnexComponents()) {
                    int crc32 = comp.getCrc32();
                    MapUtils.getOrCreateKeyArrayList(res, crc32).add(comp);
                }
            }
            cacheCrc32ToGlyphConnexeComps = res;
        }
        return cacheCrc32ToGlyphConnexeComps;
    }
    
    public List<OCRGlyphConnexeComponent> getMatchingGlyphConnexeComps(ImageData imgData) {
        List<OCRGlyphConnexeComponent> res = new ArrayList<>();
        int crc32 = IntsCRC32.crc32(imgData.getData());
        List<OCRGlyphConnexeComponent> candidates = getCrc32ToGlyphConnexeComps().get(crc32);
        if (candidates != null) {
            for(OCRGlyphConnexeComponent candidate : candidates) {
                ImageData candidateImageData = candidate.getImageData();
                if (candidateImageData.equals(imgData)) {
                    res.add(candidate);
                }
            }
        }
        return res;
    }
    
}
