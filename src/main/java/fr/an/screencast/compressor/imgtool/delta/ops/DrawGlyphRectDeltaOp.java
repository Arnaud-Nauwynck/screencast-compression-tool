package fr.an.screencast.compressor.imgtool.delta.ops;

import java.awt.image.BufferedImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.screencast.compressor.imgtool.delta.DeltaContext;
import fr.an.screencast.compressor.imgtool.delta.DeltaOperation;
import fr.an.screencast.compressor.imgtool.glyph.GlyphIndexOrCode;
import fr.an.screencast.compressor.imgtool.glyph.GlyphMRUTable;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Rect;

public class DrawGlyphRectDeltaOp extends DeltaOperation {

    /** */
    private static final long serialVersionUID = 1L;
    
    private static final Logger LOG = LoggerFactory.getLogger(DrawGlyphRectDeltaOp.class);
    
    protected final Rect rect;
    protected final GlyphIndexOrCode glyphIndexOrCode;
    
    public DrawGlyphRectDeltaOp(Rect rect, GlyphIndexOrCode glyphIndexOrCode) {
        super();
        this.rect = rect;
        this.glyphIndexOrCode = glyphIndexOrCode;
    }

    public Rect getRect() {
        return rect;
    }
    
    public GlyphIndexOrCode getGlyphIndexOrCode() {
        return glyphIndexOrCode;
    }

    @Override
    public void apply(DeltaContext context, BufferedImage dest) {
        doApply(context, dest, rect, glyphIndexOrCode);
    }

    public static void doApply(DeltaContext context, BufferedImage dest, Rect rect, GlyphIndexOrCode glyphIndexOrCode) {
        Dim destDim = new Dim(dest.getWidth(), dest.getHeight());
        final int[] destData = ImageRasterUtils.toInts(dest);
        GlyphMRUTable glyphMRUTable = context.getGlyphMRUTable();
        
        try {
            glyphMRUTable.drawGlyphFindByIndexOrCode(glyphIndexOrCode, destDim, destData, rect);
        } catch(Exception ex) {
            LOG.warn("Faile dto draw glyph ... ignore, no rethrow!", ex);
        }
    }
    
    public String toString() {
        return "Glyph[rect:" + rect 
                + " index/code:" + glyphIndexOrCode 
                + "]";
    }    

}
