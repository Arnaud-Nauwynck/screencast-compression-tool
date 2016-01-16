package fr.an.screencast.compressor.imgtool.color;

import java.awt.image.BufferedImage;

import org.junit.Assert;
import org.junit.Test;

import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.imgtool.utils.ImageTstUtils;

public class ColorLookupTableTest {

    @Test
    public void testRegisterRGBs() {
        // Prepare
        BufferedImage img = ImageTstUtils.getImageTest_1920x1080();
        int[] imgData = ImageRasterUtils.toInts(img);
        ColorLookupTable sut = new ColorLookupTable(4000); 
        // Perform
        sut.registerRGBs(imgData);
        // Post-check
        int resSize = sut.size();
        Assert.assertEquals(4587, resSize);
        // Perform..
        int maxIndex = 0;
        for(int i = 0; i < imgData.length; i++) {
            int rgbIndex = sut.rgb2Index(imgData[i]);
            Assert.assertTrue(0 <= rgbIndex);
            Assert.assertTrue(rgbIndex <= resSize+1);
            maxIndex = Math.max(maxIndex, rgbIndex);
            int checkRGB = sut.index2rgb(rgbIndex);
            Assert.assertEquals(imgData[i], checkRGB);
        }
        Assert.assertEquals(resSize-1, maxIndex);
    }
}
