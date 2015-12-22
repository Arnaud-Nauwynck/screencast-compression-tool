package fr.an.screencast.compressor.imgtool.delta.ops;

import java.awt.image.BufferedImage;

import fr.an.screencast.compressor.imgtool.delta.DeltaContext;
import fr.an.screencast.compressor.imgtool.delta.DeltaOperation;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.Rect;

public class DrawRectImageDeltaOp extends DeltaOperation {

    /** */
    private static final long serialVersionUID = 1L;
    
    private final Rect rect;
    private final int[] img;
    
    public DrawRectImageDeltaOp(Rect rect, int[] img) {
        this.rect = rect;
        this.img = img;
    }

    @Override
    public void apply(DeltaContext context, BufferedImage dest) {
        int[] destData = ImageRasterUtils.toInts(dest);
        Dim destDim = new Dim(dest.getWidth(), dest.getHeight());
        Pt destPt = rect.getFromPt();
        Dim srcDim = rect.getDim();
        Rect srcROI = Rect.newDim(rect.getDim());
        
        ImageRasterUtils.drawRectImg(destDim, destData, destPt, srcDim, img, srcROI);
    }
    
    public Rect getRect() {
        return rect;
    }

    public int[] getImg() {
        return img;
    }

    public String toString() {
        return "DrawRectImage[rect:" + rect + ", img:.." + img.length + "]";
    }
    
}
