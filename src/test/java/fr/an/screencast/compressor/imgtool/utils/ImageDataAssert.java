package fr.an.screencast.compressor.imgtool.utils;

import org.junit.Assert;

import fr.an.screencast.compressor.utils.Dim;

public class ImageDataAssert {

    public static void assertEquals(int[] expected, ImageData actual) {
        Dim dim = actual.getDim();
        assertEquals(expected, actual.getData(), dim.width, dim.height);
    }

    public static void assertEquals(int[] expected, int[] actual, int width, int height) {
        for(int y = 0, idx_xy = 0; y < height; y++) {
            for(int x = 0; x < width; x++,idx_xy++) {
                int actualValue = actual[idx_xy];
                int expectedValue = expected[idx_xy];
                if (actualValue != expectedValue) {
                    Assert.fail("expected [" + x + "][" + y + "] : " 
                            + expectedValue + "=" + RGBUtils.toString(expectedValue) 
                            + ", got " + actualValue + "=" + RGBUtils.toString(actualValue));
                }
            }
        }
    }
    
    
    public static void assertEquals(ImageData expected, ImageData actual) {
        assertEquals(expected.getData(), actual);
    }
}
