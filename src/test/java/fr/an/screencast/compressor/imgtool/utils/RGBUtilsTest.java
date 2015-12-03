package fr.an.screencast.compressor.imgtool.utils;

import org.junit.Assert;
import org.junit.Test;

public class RGBUtilsTest {

    @Test
    public void testRgb2r() {
        int value = 0x11223344;
        Assert.assertEquals(0x22, RGBUtils.redOf(value));
        Assert.assertEquals(0x33, RGBUtils.greenOf(value));
        Assert.assertEquals(0x44, RGBUtils.blueOf(value));
        Assert.assertEquals(0x11, RGBUtils.alphaOf(value));
        Assert.assertEquals(value, RGBUtils.rgb2Int(RGBUtils.redOf(value), RGBUtils.greenOf(value), RGBUtils.blueOf(value), RGBUtils.alphaOf(value)));
    }
}
