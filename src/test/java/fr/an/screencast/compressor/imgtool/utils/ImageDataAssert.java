package fr.an.screencast.compressor.imgtool.utils;

import org.junit.Assert;

import fr.an.screencast.compressor.utils.Dim;

public class ImageDataAssert {

    public static void assertEquals(int[] expected, ImageData actual) {
        Dim dim = actual.getDim();
        for(int y = 0, idx_xy = 0; y < dim.height; y++) {
            for(int x = 0; x < dim.width; x++,idx_xy++) {
                int actualValue = actual.getAt(idx_xy);
                int expectedValue = expected[idx_xy];
                if (actualValue != expectedValue) {
                    Assert.fail("expected [" + x + "][" + y + "] : " + expectedValue + ", got " + actualValue);
                }
            }
        }
    }

    public static void assertEquals(ImageData expected, ImageData actual) {
        assertEquals(expected.getData(), actual);
    }
}
