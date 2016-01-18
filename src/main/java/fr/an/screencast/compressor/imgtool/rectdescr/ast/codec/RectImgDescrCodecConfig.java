package fr.an.screencast.compressor.imgtool.rectdescr.ast.codec;

import java.util.LinkedHashMap;
import java.util.Map;

import fr.an.bitwise4j.encoder.huffman.HuffmanTable;
import fr.an.screencast.compressor.imgtool.glyph.GlyphMRUTable;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.BorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.ColumnsSplitRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.FillRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.GlyphRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.HorizontalSplitRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.LeftRightBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.LinesSplitRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RawDataRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RectImgAboveRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RoundBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.TopBottomBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.VerticalSplitRectImgDescr;

public class RectImgDescrCodecConfig {

    public static int DEFAULT_GLYPHMRUTABLE_SIZE = 2000;

    private int glyphMRUTableSize = DEFAULT_GLYPHMRUTABLE_SIZE;
    
    private Map<Class<? extends RectImgDescr>,Integer> class2frequency = defaultClass2FrequencyMap();
    
    private boolean debugAddBeginEndMarker = true;

    private boolean debugAddMarkers;
    
    // ------------------------------------------------------------------------

    public RectImgDescrCodecConfig() {
    }

    public static Map<Class<? extends RectImgDescr>, Integer> defaultClass2FrequencyMap() {
        Map<Class<? extends RectImgDescr>, Integer> res = new LinkedHashMap<Class<? extends RectImgDescr>, Integer>(); 
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
    
    public Map<Class<? extends RectImgDescr>, Integer> getClass2frequency() {
        return class2frequency;
    }

    public void setClass2frequency(Map<Class<? extends RectImgDescr>, Integer> class2frequency) {
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

    public HuffmanTable<Class<? extends RectImgDescr>> createHuffmanTableForClass2Frequency() {
        HuffmanTable<Class<? extends RectImgDescr>> res = new HuffmanTable<Class<? extends RectImgDescr>>();
        for(Map.Entry<Class<? extends RectImgDescr>, Integer> e : class2frequency.entrySet()) {
            res.addSymbol(e.getKey(), e.getValue());
        }
                 
        res.compute();
        return res;
    }

    public GlyphMRUTable createGlyphMRUTable() {
        return new GlyphMRUTable(glyphMRUTableSize);
    }

}
