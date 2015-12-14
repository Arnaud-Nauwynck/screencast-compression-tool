package fr.an.screencast.compressor.imgtool.delta.ops;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import fr.an.screencast.compressor.imgtool.delta.DeltaContext;
import fr.an.screencast.compressor.imgtool.delta.DeltaOperation;
import fr.an.screencast.compressor.imgtool.delta.IntImageLRUChangeHistory;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.Rect;

public class RestorePrevImageRectDeltaOp extends DeltaOperation {

    /** */
    private static final long serialVersionUID = 1L;
    
    private final Rect rect;
    private final int prevFrameOffset; // 0 if copy from self image
    private final Pt prevFrameLocation;
    
    private final transient List<Rect> detailedMergeRects;
    
    public RestorePrevImageRectDeltaOp(Rect rect, int prevFrameOffset, Pt prevFrameLocation, List<Rect> detailedMergeRects) {
        this.rect = rect;
        this.prevFrameOffset = prevFrameOffset;
        this.prevFrameLocation = prevFrameLocation;
        this.detailedMergeRects = detailedMergeRects;
    }

    @Override
    public void apply(DeltaContext context, BufferedImage dest) {
        if (prevFrameOffset == 0) {
            // copy move area from same image
            Graphics2D g2D = dest.createGraphics();
            int sx = prevFrameLocation.x, sy = prevFrameLocation.x;
            g2D.drawImage(dest, rect.fromX, rect.fromY, rect.toX, rect.toY, 
                sx, sy, sx + rect.getWidth(), sy+rect.getHeight(), null);
        } else if (prevFrameOffset == -1) {
            // copy from previous image
            Graphics2D g2D = dest.createGraphics();
            int sx = prevFrameLocation.x, sy = prevFrameLocation.x;
            g2D.drawImage(context.getPrevImage(), rect.fromX, rect.fromY, rect.toX, rect.toY, 
                sx, sy, sx + rect.getWidth(), sy+rect.getHeight(), null);
        } else {
            // copy from LRU color per pixels 
            IntImageLRUChangeHistory imageLRUChange = context.getImageLRUChangeHistory();
            int[] destData = ImageRasterUtils.toInts(dest);
            int prevFrameIndex = context.getFrameIndex() - prevFrameOffset;
            imageLRUChange.tryRestoreFrameImageRect(destData, prevFrameIndex, rect, prevFrameLocation);
        }
    }
    
    public Rect getRect() {
        return rect;
    }

    public int getPrevFrameOffset() {
        return prevFrameOffset;
    }

    public Pt getPrevFrameLocation() {
        return prevFrameLocation;
    }

    public List<Rect> getDetailedMergeRects() {
        return detailedMergeRects;
    }


    
    public String toString() {
        return "RestorePrevImageRect[rect:" + rect + ", frameOffset:" + prevFrameOffset + ", prevLoc:" + prevFrameLocation + "]";
    }
    
}
