package fr.an.screencast.compressor.imgtool.delta.ops;

import java.awt.image.BufferedImage;

import fr.an.screencast.compressor.imgtool.delta.DeltaContext;
import fr.an.screencast.compressor.imgtool.delta.DeltaOperation;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.screencast.compressor.utils.Rect;

public class FillRectDeltaOp extends DeltaOperation {

    /** */
    private static final long serialVersionUID = 1L;
    
    private final Rect rect;
    private final int fillColor;
    
    public FillRectDeltaOp(Rect rect, int fillColor) {
        this.rect = rect;
        this.fillColor = fillColor;
    }

    @Override
    public void apply(DeltaContext context, BufferedImage dest) {
        final int width = dest.getWidth();
        final int[] imgData = ImageRasterUtils.toInts(dest);
        int idx = rect.fromY * width + rect.fromX;
        final int incIdxY = width - rect.toX + rect.fromX;
        for(int y = rect.fromY; y < rect.toY; y++,idx+=incIdxY) {
            for(int x = rect.fromX; x < rect.toX; x++,idx++) {
                imgData[idx] = fillColor;
            }
        }
    }
    
    public String toString() {
        return "FillRect[rect:" + rect + ", color:" + RGBUtils.toString(fillColor) + "]";
    }
    
}

