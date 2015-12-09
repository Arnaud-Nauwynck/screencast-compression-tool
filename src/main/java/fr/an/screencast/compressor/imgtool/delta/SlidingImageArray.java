package fr.an.screencast.compressor.imgtool.delta;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import fr.an.screencast.compressor.utils.Dim;

public class SlidingImageArray {

    private final int slidingLen;
    private final Dim dim;
    
    private BufferedImage[] prevImage;

    // ------------------------------------------------------------------------
    
    public SlidingImageArray(int slidingLen, Dim dim, int intImageType) { 
        this.slidingLen = slidingLen;
        this.dim = dim;

        prevImage = new BufferedImage[slidingLen];
        for (int i = 0; i < slidingLen; i++) {
            prevImage[i] = new BufferedImage(dim.width, dim.height, intImageType);
        }
    }

    // ------------------------------------------------------------------------
    
    public void slide(Image image) {
        // shift rotate pointers for internal images
        BufferedImage oldestImage = prevImage[slidingLen-1];
        for (int i = 0; i < slidingLen-1; i++) {
            prevImage[i+1] = prevImage[i];
        }
        prevImage[0] = oldestImage;
        // copy data (optionnaly convert) for last image (do not keep point to externally managed data)
        Graphics2D g2d = prevImage[0].createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
    }

    // ------------------------------------------------------------------------

    public int getSlidingLen() {
        return slidingLen;
    }
    
    public Dim getDim() {
        return dim;
    }

    public BufferedImage[] getPrevImage() {
        return prevImage;
    }

    // ------------------------------------------------------------------------
    
    @Override
    public String toString() {
        return "SlidingImageArray [slidingLen=" + slidingLen + ", prevImage=" + prevImage + "]";
    }
        
}
