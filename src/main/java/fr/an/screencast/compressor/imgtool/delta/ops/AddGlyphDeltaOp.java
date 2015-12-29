package fr.an.screencast.compressor.imgtool.delta.ops;

import java.awt.image.BufferedImage;

import fr.an.screencast.compressor.imgtool.delta.DeltaContext;
import fr.an.screencast.compressor.imgtool.delta.DeltaOperation;
import fr.an.screencast.compressor.imgtool.glyph.GlyphMRUTable;
import fr.an.screencast.compressor.imgtool.glyph.GlyphMRUTable.GlyphMRUNode;
import fr.an.screencast.compressor.imgtool.utils.IntsCRC32;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Rect;

public class AddGlyphDeltaOp extends DeltaOperation {

    /** */
    private static final long serialVersionUID = 1L;
    
    private final Dim glyphDim;
    private final int[] glyphData;

    public AddGlyphDeltaOp(Dim glyphDim, int[] glyphData) {
        this.glyphDim = glyphDim;
        this.glyphData = glyphData;
    }

    @Override
    public void apply(DeltaContext context, BufferedImage dest) {
        doApply(context, glyphDim, glyphData);
    }

    public static GlyphMRUNode doApply(DeltaContext context, Dim glyphDim, int[] glyphData) {
        GlyphMRUTable glyphMRUTable = context.getGlyphMRUTable();
        int crc = IntsCRC32.crc32(glyphData, 0, glyphData.length);
        Rect dimAsRect = Rect.newDim(glyphDim);
        GlyphMRUNode res = glyphMRUTable.addGlyph(glyphDim, glyphData, dimAsRect, crc);
        return res;
    }
    
    public Dim getGlyphDim() {
        return glyphDim;
    }

    public int[] getGlyphData() {
        return glyphData;
    }

    public String toString() {
        return "AddGlyph[dim:" + glyphDim + ", data:..]";
    }    

}
