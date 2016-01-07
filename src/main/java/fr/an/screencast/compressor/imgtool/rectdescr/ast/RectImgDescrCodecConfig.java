package fr.an.screencast.compressor.imgtool.rectdescr.ast;

import java.util.LinkedHashMap;
import java.util.Map;

import fr.an.screencast.compressor.imgtool.glyph.GlyphMRUTable;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.BorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.ColumnsSplitRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.FillRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.GlyphRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.HorizontalSplitRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.LeftRightBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.LinesSplitRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RawDataRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RectImgAboveRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RectImgDescription;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RoundBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.TopBottomBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.VerticalSplitRectImgDescr;
import fr.an.util.encoder.huffman.HuffmanTable;

public class RectImgDescrCodecConfig {

    public static int DEFAULT_GLYPHMRUTABLE_SIZE = 2000;

    private int glyphMRUTableSize = DEFAULT_GLYPHMRUTABLE_SIZE;
    
    private Map<Class<? extends RectImgDescription>,Integer> class2frequency = defaultClass2FrequencyMap();
    
    private boolean debugAddBeginEndMarker = true;

    private boolean debugAddMarkers;
    
    // ------------------------------------------------------------------------

    public RectImgDescrCodecConfig() {
    }

    public static Map<Class<? extends RectImgDescription>, Integer> defaultClass2FrequencyMap() {
        Map<Class<? extends RectImgDescription>, Integer> res = new LinkedHashMap<Class<? extends RectImgDescription>, Integer>(); 
        res.put(FillRectImgDescr.class, 3);
        res.put(RoundBorderRectImgDescr.class, 1);
        res.put(BorderRectImgDescr.class, 2);
        res.put(TopBottomBorderRectImgDescr.class, 6);
        res.put(LeftRightBorderRectImgDescr.class, 5);
        res.put(VerticalSplitRectImgDescr.class, 2);
        res.put(HorizontalSplitRectImgDescr.class, 1);
        res.put(LinesSplitRectImgDescr.class, 11);
        res.put(ColumnsSplitRectImgDescr.class, 10);
        res.put(RawDataRectImgDescr.class, 1);
        res.put(GlyphRectImgDescr.class, 20);
        res.put(RectImgAboveRectImgDescr.class, 1);

        return res;
    }

    // ------------------------------------------------------------------------
    
    public int getGlyphMRUTableSize() {
        return glyphMRUTableSize;
    }

    public void setGlyphMRUTableSize(int glyphMRUTableSize) {
        this.glyphMRUTableSize = glyphMRUTableSize;
    }
    
    public Map<Class<? extends RectImgDescription>, Integer> getClass2frequency() {
        return class2frequency;
    }

    public void setClass2frequency(Map<Class<? extends RectImgDescription>, Integer> class2frequency) {
        this.class2frequency = class2frequency;
    }
    
    public boolean isDebugAddMarkers() {
        return debugAddMarkers;
    }

    public void setDebugAddMarkers(boolean debugAddMarkers) {
        this.debugAddMarkers = debugAddMarkers;
    }

    public boolean isDebugAddBeginEndMarker() {
        return debugAddBeginEndMarker;
    }

    public void setDebugAddBeginEndMarker(boolean debugAddBeginEndMarker) {
        this.debugAddBeginEndMarker = debugAddBeginEndMarker;
    }

    public HuffmanTable<Class<? extends RectImgDescription>> createHuffmanTableForClass2Frequency() {
        HuffmanTable<Class<? extends RectImgDescription>> res = new HuffmanTable<Class<? extends RectImgDescription>>();
        for(Map.Entry<Class<? extends RectImgDescription>, Integer> e : class2frequency.entrySet()) {
            res.addSymbol(e.getKey(), e.getValue());
        }
                 
        res.compute();
        return res;
    }

    public GlyphMRUTable createGlyphMRUTable() {
        return new GlyphMRUTable(glyphMRUTableSize);
    }

}
