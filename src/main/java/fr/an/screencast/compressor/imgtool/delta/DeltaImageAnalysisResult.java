package fr.an.screencast.compressor.imgtool.delta;

import java.awt.image.BufferedImage;

import fr.an.screencast.compressor.utils.Dim;

public class DeltaImageAnalysisResult {

    BufferedImage diffImage;

    BufferedImage deltaImage;

    // ------------------------------------------------------------------------

    public DeltaImageAnalysisResult(Dim dim, int intImageType) {
        this.diffImage = new BufferedImage(dim.width, dim.height, intImageType);
        this.deltaImage = new BufferedImage(dim.width, dim.height, intImageType);
    }

    // ------------------------------------------------------------------------

    
    public BufferedImage getDiffImage() {
        return diffImage;
    }

    public BufferedImage getDeltaImage() {
        return deltaImage;
    }

}
