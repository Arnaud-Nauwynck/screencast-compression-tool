package fr.an.screencast.compressor.imgtool.color;

import org.junit.Assert;
import org.junit.Test;

import fr.an.screencast.compressor.imgtool.utils.RGBUtils;

public class ColorBitsReducerTest {

    @Test
    public void testReduceRGBLeastSignificantBits() {
        // Prepare
        int nbRemovedColorBits = 4; // => 2^4=16
        ColorBitsReducer colorBitsReducer = new ColorBitsReducer(1, nbRemovedColorBits);
        // Perform
        for (int i = 0; i < 255; i++) {
            int greyI = RGBUtils.greyRgb2Int(i);
            int reducedGrey = colorBitsReducer.reduceRGBLeastSignificantBits(greyI);
            // System.out.println("rgb " + i + " => " + RGBUtils.toString(reducedGrey));
            int reduced_r = RGBUtils.redOf(reducedGrey);
            int reduced_g = RGBUtils.greenOf(reducedGrey);
            int reduced_b = RGBUtils.blueOf(reducedGrey);
            Assert.assertEquals((i/16) * 16, reduced_r);
            Assert.assertEquals(0, reduced_r % 16);
            Assert.assertEquals(0, reduced_g % 16);
            Assert.assertEquals(0, reduced_b % 16);
        }
        // Post-check
    }

}
