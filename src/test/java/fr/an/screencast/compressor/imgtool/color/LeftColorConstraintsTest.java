package fr.an.screencast.compressor.imgtool.color;

import java.awt.image.BufferedImage;

import org.junit.Test;

import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.imgtool.utils.ImageTstUtils;
import fr.an.screencast.compressor.utils.Dim;

public class LeftColorConstraintsTest {

    private static boolean DEBUG = false;
    
    @Test
    public void testAnalyseImgColors() {
        // Prepare
        BufferedImage img = ImageTstUtils.getImageTest_1920x1080();
        Dim dim = new Dim(img.getWidth(), img.getHeight());
        int[] imgData = ImageRasterUtils.toInts(img);
        ColorLookupTable colorLookupTable = new ColorLookupTable(4000); 
        colorLookupTable.registerRGBs(imgData);
        LeftColorConstraints sut = new LeftColorConstraints(colorLookupTable);
        // Perform
        sut.analyseImgColors(dim, imgData);
        // Post-check
        if (DEBUG) {
            System.out.println(sut);
        }
    }
    
}
