package fr.an.screencast.compressor;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class SlidingImageArray {

    private final int slidingLen;
    private final int width, height;
    
    private BufferedImage[] prevImage;

    // ------------------------------------------------------------------------
    
    public SlidingImageArray(int slidingLen, int width, int height, int intImageType) { 
        this.slidingLen = slidingLen;
        this.width = width;
        this.height = height;

        prevImage = new BufferedImage[slidingLen];
        for (int i = 0; i < slidingLen; i++) {
            prevImage[i] = new BufferedImage(width, height, intImageType);
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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
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
