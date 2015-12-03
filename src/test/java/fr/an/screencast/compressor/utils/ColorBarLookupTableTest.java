package fr.an.screencast.compressor.utils;

import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import fr.an.screencast.compressor.imgtool.utils.RGBUtils;

public class ColorBarLookupTableTest {

    @Test
    public void testInterpolate() {
        // Prepare
        URL imgURL =  ColorBarLookupTable.class.getClassLoader().getResource("colorbar-cold-hot.png");
        ColorBarLookupTable sut = ColorBarLookupTable.newFromFile(imgURL, 1);
        // Perform
        for (int i = 0; i < 1000; i++) {
            sut.interpolateRGB(i, 0, 1000);
        }
        // Post-check
        int c0 = sut.interpolateRGB(0, 0, 100);
        Assert.assertEquals(RGBUtils.rgb2Int(0,18,58), c0); // dark blue
        int c50 = sut.interpolateRGB(50, 0, 100);
        Assert.assertEquals(RGBUtils.rgb2Int(144,28,39), c50); // red,orange
        int c100 = sut.interpolateRGB(100, 0, 100);
        Assert.assertEquals(RGBUtils.rgb2Int(255,254,13), c100); // yellow
    }
}
