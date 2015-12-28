package fr.an.screencast.compressor.imgtool.delta.ops;

import java.awt.image.BufferedImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.screencast.compressor.imgtool.delta.DeltaContext;
import fr.an.screencast.compressor.imgtool.delta.DeltaOperation;
import fr.an.screencast.compressor.imgtool.glyph.GlyphMRUTable;
import fr.an.screencast.compressor.imgtool.glyph.GlyphMRUTable.GlyphMRUNode;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Rect;

public class GlyphRectDeltaOp extends DeltaOperation {

    /** */
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(GlyphRectDeltaOp.class);
    
    protected final Rect rect;
    protected final int glyphId;
    
    public GlyphRectDeltaOp(Rect rect, int glyphId) {
        this.rect = rect;
        this.glyphId = glyphId;
    }
    
    public Rect getRect() {
        return rect;
    }

    public int getGlyphId() {
        return glyphId;
    }

    @Override
    public void apply(DeltaContext context, BufferedImage dest) {
        Dim destDim = new Dim(dest.getWidth(), dest.getHeight());
        final int[] destData = ImageRasterUtils.toInts(dest);
        
        GlyphMRUTable glyphMRUTable = context.getGlyphMRUTable();
        GlyphMRUNode glyphNode = glyphMRUTable.findGlyphById(glyphId);
        if (glyphNode == null) {
            LOG.warn("glyph not found by id:" + glyphId + " ... IGNORE, can not draw!");
            return;
        }

        final int[] glyphData = glyphNode.getData();
        Dim glyphDim = glyphNode.getDim();
        if (!glyphDim.equals(rect.getDim())) {
            LOG.warn("glyph id:" + glyphId + " dim:" + glyphDim + " expected rect dim:" + rect.getDim() + "... IGNORE, can not draw!");
            return;
        }
        Rect glyphROI = Rect.newDim(glyphDim); 
        ImageRasterUtils.drawRectImg(destDim, destData, rect.getFromPt(), glyphDim, glyphData, glyphROI);
    }
    
    public String toString() {
        return "Glyph[rect:" + rect + ", glyphId:" + glyphId + "]";
    }    

}
