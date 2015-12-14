package fr.an.screencast.compressor.imgtool.delta;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
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
import fr.an.screencast.compressor.utils.RectUtils;

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

            // update LRU color change per pixel
            for(Rect rect : rects) {
                imageLRUChangeHistory.addTimeValues(frameIndex, imageData, rect);
            }
            
            
            for(int i = 0; i < rects.size(); i++) {
                Rect rect = rects.get(i);
                FrameRectDelta rectDelta = new FrameRectDelta(frameDelta, rect);
                frameDelta.addFrameRectDelta(rectDelta);

                
                // TODO ... add more analysis in rect ...
                
                // check if rect can be (fully) restored from "imageLRUChangeHistory", using per-pixel LRU change history
                // (much larger history than using "slidingImages", using global image sliding history) 
                Pt restoreFromSamePt = rect.getFromPt();
                List<RectRestorableResult> restorePerFrames = imageLRUChangeHistory.computeRestorableNthPrevFrame(frameIndex, imageData, rect, restoreFromSamePt, 0, 0);
                if (restorePerFrames != null && !restorePerFrames.isEmpty()) {
                    RectRestorableResult foundExactRestore = null;
                    for(RectRestorableResult e : restorePerFrames) {
                        if (e.countDiff == 0 && e.countUnrestorable == 0) {
                            foundExactRestore = e;
                            break;
                        }
                    }
                    if (foundExactRestore != null) {
                        Rect mergeRestorableRect = rect;
                        Rect mergeRect = new Rect();
                        List<Rect> detailedMergeRects = new ArrayList<Rect>();
                        int restoreFrameIndex = foundExactRestore.frameIndex;
                        // try to merge small rects with same restore operation
                        Rect[] mergeComplRects = RectUtils.newArray(4);
                        List<Pt> unrestorablePts = new ArrayList<Pt>();
                        loop_mergeOther: for (int j = i+1; j < rects.size(); j++) {
                            Rect otherRect = rects.get(j);
                            mergeRect = RectUtils.enclosingRect(mergeRect, mergeRestorableRect, otherRect);
                            if (mergeRect.findFirstContainedPt(unrestorablePts) != null) {
                                continue;
                            }
                            RectRestorableResult restorableOther = imageLRUChangeHistory.computeRestorableRectFrame(restoreFrameIndex, imageData, 
                                otherRect, otherRect.getFromPt(), 0, 0);
                            if (restorableOther != null && restorableOther.isExactRestoration() 
                                    && restorableOther.frameIndex == restoreFrameIndex) {
                                // can merge if complements rect are also restorable
                                boolean canMerge = true;
                                RectUtils.complementOfEnclosing(mergeComplRects, mergeRestorableRect, otherRect);
                                for(Rect mergeComplRect : mergeComplRects) {
                                    if (mergeComplRect.isEmpty()) {
                                        break;
                                    }
                                    RectRestorableResult complRestorable = imageLRUChangeHistory.computeRestorableRectFrame(restoreFrameIndex, imageData, 
                                        mergeComplRect, mergeComplRect.getFromPt(), 0, 0);
                                    if (complRestorable.isExactRestoration() 
                                            && complRestorable.frameIndex == restoreFrameIndex) {
                                    } else {
                                        if (complRestorable.firstUnrestorablePt != null) {
                                            unrestorablePts.add(complRestorable.firstUnrestorablePt);
                                            canMerge = false;
                                            continue loop_mergeOther;
                                        }
                                    }
                                }
                                if (canMerge) {
                                    mergeRestorableRect = mergeRect.cloneRect();
                                    rects.remove(j);
                                    detailedMergeRects.add(otherRect);
                                    j--;
                                }
                            }
                        }
                        rectDelta.addDeltaOperation(new RestorePrevImageRectDeltaOp(mergeRestorableRect, restoreFrameIndex, restoreFromSamePt, detailedMergeRects));
                        continue;
                    }
                }
                
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

}
