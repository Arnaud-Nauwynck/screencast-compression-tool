package fr.an.screencast.compressor;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class DeltaImageAnalysisResult {

    BufferedImage diffImage;
    int[] diffImageDataInts;

    BufferedImage deltaImage;
    int[] deltaImageDataInts;

    // ------------------------------------------------------------------------

    public DeltaImageAnalysisResult(int width, int height, int intImageType) {
        diffImage = new BufferedImage(width, height, intImageType);
        diffImageDataInts = ((DataBufferInt) diffImage.getRaster().getDataBuffer()).getData();
        
        deltaImage = new BufferedImage(width, height, intImageType);
        deltaImageDataInts = ((DataBufferInt) deltaImage.getRaster().getDataBuffer()).getData();
    }

    // ------------------------------------------------------------------------

    
    public BufferedImage getDiffImage() {
        return diffImage;
    }

    public int[] getDiffImageDataInts() {
        return diffImageDataInts;
    }

    public BufferedImage getDeltaImage() {
        return deltaImage;
    }

    public int[] getDeltaImageDataInts() {
        return deltaImageDataInts;
    }

    
}
