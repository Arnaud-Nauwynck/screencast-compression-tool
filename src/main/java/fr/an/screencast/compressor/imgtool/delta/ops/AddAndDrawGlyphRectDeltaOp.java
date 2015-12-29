package fr.an.screencast.compressor.imgtool.delta.ops;

import java.awt.image.BufferedImage;

import fr.an.screencast.compressor.imgtool.delta.DeltaContext;
import fr.an.screencast.compressor.imgtool.delta.DeltaOperation;
import fr.an.screencast.compressor.imgtool.glyph.GlyphMRUTable.GlyphMRUNode;
import fr.an.screencast.compressor.utils.Rect;

public class AddAndDrawGlyphRectDeltaOp extends DeltaOperation {

    /** */
    private static final long serialVersionUID = 1L;
    
    protected final Rect rect;
    private final int[] glyphData;
    
    public AddAndDrawGlyphRectDeltaOp(Rect rect, int[] glyphData) {
        this.rect = rect;
        this.glyphData = glyphData;
    }

    @Override
    public void apply(DeltaContext context, BufferedImage dest) {
        GlyphMRUNode glyphNode = AddGlyphDeltaOp.doApply(context, rect.getDim(), glyphData);
        DrawGlyphRectDeltaOp.doApply(context, dest, rect, glyphNode.getIndexOrCode());
    }

    public Rect getRect() {
        return rect;
    }
    
    public int[] getGlyphData() {
        return glyphData;
    }

    public String toString() {
        return "Glyph[rect:" + rect 
                + " data: ... " + ((glyphData != null)? glyphData.length : 0) + " bytes" 
                + "]";
    }    

}
