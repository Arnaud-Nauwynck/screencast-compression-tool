package fr.an.screencast.compressor.imgtool.delta;

import java.awt.image.BufferedImage;
import java.util.List;

import fr.an.screencast.compressor.imgtool.delta.IntImageLRUChangeHistory.RectRestorableResult;
import fr.an.screencast.compressor.imgtool.delta.ops.RestorePrevImageRectDeltaOp;
import fr.an.screencast.compressor.imgtool.search.BinaryImageEnclosingRectsFinder;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.imgtool.utils.RasterImageFunction;
import fr.an.screencast.compressor.imgtool.utils.RasterImageFunctions;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.Rect;

public class DeltaImageAnalysisProcessor {

    private final Dim dim;
    private SlidingImageArray slidingImages;
    private BinaryImageEnclosingRectsFinder binaryImageRectsFinder;

    private DeltaImageAnalysisResult deltaResult;
    
    private IntImageLRUChangeHistory imageLRUChangeHistory;
    
    // ------------------------------------------------------------------------

    public DeltaImageAnalysisProcessor(DeltaImageAnalysisResult deltaResult, Dim dim, 
            int prevSlidingLen, int perPixelLRUHistory) {
        this.deltaResult = deltaResult;
        this.dim = dim;
        this.slidingImages = new SlidingImageArray(prevSlidingLen, dim, BufferedImage.TYPE_INT_RGB);
        this.binaryImageRectsFinder = new BinaryImageEnclosingRectsFinder(dim);
        this.imageLRUChangeHistory = new IntImageLRUChangeHistory(dim, perPixelLRUHistory); 
    }
    
    // ------------------------------------------------------------------------

    
    public void processImage(int frameIndex, BufferedImage imageRGB) {
        slidingImages.slide(imageRGB);
        
        BufferedImage prevImageRGB = slidingImages.getPrevImage()[1];
        final int[] imageData = ImageRasterUtils.toInts(imageRGB);
        
        RasterImageFunction binaryDiff = RasterImageFunctions.binaryDiff(dim, imageRGB, prevImageRGB);
        
        // compute enclosing rectangles containing differences between image and previous image
        List<Rect> rects = binaryImageRectsFinder.findEnclosingRects(binaryDiff);

        if (rects != null && !rects.isEmpty()) {
            FrameDelta frameDelta = new FrameDelta(frameIndex);

            for(Rect rect : rects) {
                FrameRectDelta rectDelta = new FrameRectDelta(frameDelta, rect);
                frameDelta.addFrameRectDelta(rectDelta);

                // update LRU color change per pixel
                imageLRUChangeHistory.addTimeValues(frameIndex, imageData, rect);
                
                // TODO ... add more analysis in rect ...
                
                // check if rect can be (fully) restored from "imageLRUChangeHistory", using per-pixel LRU change history
                // (much larger history than using "slidingImages", using global image sliding history) 
                Pt restoreFromSamePt = new Pt(rect.fromX, rect.fromY);
                List<RectRestorableResult> restorePerFrames = imageLRUChangeHistory.computeRestorableNthPrevFrame(frameIndex, imageData, rect, restoreFromSamePt, 0, 0);
                // TODO
//                if (restorePerFrames != null && !restorePerFrames.isEmpty()) {
//                    for(RectRestorableResult restorePerFrame : restorePerFrames) {
//                        restorePerFrame.
//                    }
//                    rectDelta.addDeltaOperation(new RestorePrevImageRectDeltaOp(rect, foundRestoreFrameIndex, restoreFromSamePt));
//                }
                
                // check if rect can be re-synthethised using primitive graphical operation:
                // - fillRectangle()  (drawLine()=degenerated rectangle of thick 1 / border)
                // - drawRectangle() with border and unchanged content
                // - drawText()
                
                // check if rect can be painted using already seen glyph  (using connex component detection / database of known glyphs)
                // - drawGlyph()
                // ...
                
                // check if rect can be painted as a video inverse of previous frame
                
                // check if rect can be painted from a shift (scrolling) of previous frame
                
            
            }
            deltaResult.addFrameDelta(frameDelta);
        }
    }

    private int findLRURestoreRect(int[] imageData, Rect rect) {
        // TODO Auto-generated method stub
        return 0;
    }

}
