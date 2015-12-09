package fr.an.screencast.compressor.imgtool.delta;

import java.awt.image.BufferedImage;
import java.util.List;

import fr.an.screencast.compressor.imgtool.search.BinaryImageEnclosingRectsFinder;
import fr.an.screencast.compressor.imgtool.utils.RasterImageFunction;
import fr.an.screencast.compressor.imgtool.utils.RasterImageFunctions;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Rect;

public class DeltaImageAnalysisProcessor {

    private final Dim dim;
    private SlidingImageArray slidingImages;
    private BinaryImageEnclosingRectsFinder binaryImageRectsFinder;

    private DeltaImageAnalysisResult deltaResult;
    
    // ------------------------------------------------------------------------

    public DeltaImageAnalysisProcessor(Dim dim, int prevSlidingLen, DeltaImageAnalysisResult deltaResult) {
        super();
        this.dim = dim;
        this.slidingImages = new SlidingImageArray(prevSlidingLen, dim, BufferedImage.TYPE_INT_RGB);
        this.binaryImageRectsFinder = new BinaryImageEnclosingRectsFinder(dim);
        this.deltaResult = deltaResult;
    }
    
    // ------------------------------------------------------------------------

    
    public void processImage(int frameIndex, BufferedImage imageRGB) {
        slidingImages.slide(imageRGB);
        
        BufferedImage prevImageRGB = slidingImages.getPrevImage()[1];
        
        RasterImageFunction binaryDiff = RasterImageFunctions.binaryDiff(dim, imageRGB, prevImageRGB);
        
        // compute enclosing rectangles containing differences between image and previous image
        List<Rect> rects = binaryImageRectsFinder.findEnclosingRects(binaryDiff);

        if (! rects.isEmpty()) {
            FrameDelta frameDelta = new FrameDelta(frameIndex);
            frameDelta.addFrameRectDeltas(rects);
            
            deltaResult.addFrameDelta(frameDelta);
        }
    }

}
